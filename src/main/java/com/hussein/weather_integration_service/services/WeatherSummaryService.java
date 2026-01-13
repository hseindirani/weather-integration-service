package com.hussein.weather_integration_service.services;

import com.hussein.weather_integration_service.clients.OpenWeatherClient;
import com.hussein.weather_integration_service.dto.openweather.OpenWeatherForecastResponse;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import com.hussein.weather_integration_service.cache.WeatherForecastCache;
import org.springframework.web.client.RestClientResponseException;




import java.util.List;

@Service
public class WeatherSummaryService {
    private final OpenWeatherClient openWeatherClient;
    private final WeatherForecastCache weatherForecastCache;

    public WeatherSummaryService(OpenWeatherClient openWeatherClient, WeatherForecastCache weatherForecastCache) {
        this.openWeatherClient = openWeatherClient;
        this.weatherForecastCache = weatherForecastCache;
    }



    public List<String> getMatchingLocations(String unit, int temperatureThreshold, List<Long> locationIds) {

        String openWeatherUnit = unit.equalsIgnoreCase("celsius") ? "metric" : "imperial";

        return locationIds.stream()
                .filter(locationId -> {

                    String cacheKey = "forecast:" + locationId + ":" + openWeatherUnit;

                    OpenWeatherForecastResponse response = weatherForecastCache.getFresh(cacheKey);

                    if (response == null) {
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
                                return false; // skip this location
                            }
                        } catch (Exception ex) {
                            OpenWeatherForecastResponse stale = weatherForecastCache.getStale(cacheKey);
                            if (stale != null) {
                                response = stale;
                            } else {
                                return false; // skip this location
                            }
                        }
                    }

                    if (response == null || response.list() == null || response.list().isEmpty()) {
                        return false;
                    }

                    Double tomorrowMax = getTomorrowMaxTemp(response);
                    return tomorrowMax != null && tomorrowMax > temperatureThreshold;
                })
                .map(String::valueOf)
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
