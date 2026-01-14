package com.hussein.weather_integration_service.services;

import com.hussein.weather_integration_service.clients.OpenWeatherClient;
import com.hussein.weather_integration_service.dto.WeatherSummaryMatchDto;
import com.hussein.weather_integration_service.dto.openweather.OpenWeatherForecastResponse;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import com.hussein.weather_integration_service.cache.WeatherForecastCache;
import org.springframework.web.client.RestClientResponseException;
import com.hussein.weather_integration_service.ratelimit.DailyApiBudget;




import java.util.List;

@Service
public class WeatherSummaryService {
    private final OpenWeatherClient openWeatherClient;
    private final WeatherForecastCache weatherForecastCache;
    private  final  DailyApiBudget dailyApiBudget;

    public WeatherSummaryService(OpenWeatherClient openWeatherClient, WeatherForecastCache weatherForecastCache,DailyApiBudget dailyApiBudget) {
        this.openWeatherClient = openWeatherClient;
        this.weatherForecastCache = weatherForecastCache;
        this.dailyApiBudget = dailyApiBudget;
    }



    public List<WeatherSummaryMatchDto> getMatchingLocations(String unit, int temperatureThreshold, List<Long> locationIds) {

        String openWeatherUnit = unit.equalsIgnoreCase("celsius") ? "metric" : "imperial";
        String tomorrowDate = LocalDate.now(ZoneOffset.UTC).plusDays(1).toString();

        return locationIds.stream()
                .map(locationId -> {

                    String cacheKey = "forecast:" + locationId + ":" + openWeatherUnit;

                    OpenWeatherForecastResponse response = weatherForecastCache.getFresh(cacheKey);

                    if (response == null) {

                        // Budget check BEFORE calling OpenWeather
                        if (!dailyApiBudget.tryConsume()) {
                            OpenWeatherForecastResponse stale = weatherForecastCache.getStale(cacheKey);
                            if (stale != null) {
                                response = stale;
                            } else {
                                return null; // skip this location
                            }
                        } else {
                            try {
                                response = openWeatherClient.fetchForecast(locationId, openWeatherUnit);
                                if (response != null) {
                                    weatherForecastCache.put(cacheKey, response, 1800);
                                }
                            } catch (RestClientResponseException ex) {
                                OpenWeatherForecastResponse stale = weatherForecastCache.getStale(cacheKey);
                                if (stale != null) {
                                    response = stale;
                                } else {
                                    return null;
                                }
                            } catch (Exception ex) {
                                OpenWeatherForecastResponse stale = weatherForecastCache.getStale(cacheKey);
                                if (stale != null) {
                                    response = stale;
                                } else {
                                    return null;
                                }
                            }
                        }
                    }

                    if (response == null || response.list() == null || response.list().isEmpty()) {
                        return null;
                    }

                    Double tomorrowMax = getTomorrowMaxTemp(response);

                    if (tomorrowMax != null && tomorrowMax > temperatureThreshold) {
                        return new WeatherSummaryMatchDto(
                                String.valueOf(locationId),
                                tomorrowDate,
                                tomorrowMax
                        );
                    }

                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }


    private Double getTomorrowMaxTemp(OpenWeatherForecastResponse response) {
        LocalDate tomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1);

        return response.list().stream()
                .filter(item -> {
                    LocalDate itemDate = Instant.ofEpochSecond(item.dt())
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate();
                    return itemDate.equals(tomorrow);
                })
                .map(item -> item.main().temp())
                .max(Double::compareTo)
                .orElse(null);
    }


}
