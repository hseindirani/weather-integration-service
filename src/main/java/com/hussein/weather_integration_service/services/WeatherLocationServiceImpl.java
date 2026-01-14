package com.hussein.weather_integration_service.services;

import com.hussein.weather_integration_service.clients.OpenWeatherClient;
import com.hussein.weather_integration_service.cache.WeatherForecastCache;
import com.hussein.weather_integration_service.dto.DailyTemperatureDto;
import com.hussein.weather_integration_service.dto.openweather.OpenWeatherForecastResponse;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientResponseException;
import com.hussein.weather_integration_service.ratelimit.DailyApiBudget;



import java.util.List;

@Service
public class WeatherLocationServiceImpl implements WeatherLocationService {

    private final OpenWeatherClient openWeatherClient;
    private final WeatherForecastCache weatherForecastCache;
    private final DailyApiBudget dailyApiBudget;

    public WeatherLocationServiceImpl(OpenWeatherClient openWeatherClient, WeatherForecastCache weatherForecastCache,DailyApiBudget dailyApiBudget) {
        this.openWeatherClient = openWeatherClient;
        this.weatherForecastCache = weatherForecastCache;
        this.dailyApiBudget = dailyApiBudget;
    }

    @Override
    public List<DailyTemperatureDto> getNext5DaysMaxTemps(long locationId, String unit) {

        String openWeatherUnit = unit.equalsIgnoreCase("celsius") ? "metric" : "imperial";
        String cacheKey = "forecast:" + locationId + ":" + openWeatherUnit;

        OpenWeatherForecastResponse response = weatherForecastCache.getFresh(cacheKey);

        if (response == null) {

            // 1) Budget check BEFORE calling OpenWeather
            if (!dailyApiBudget.tryConsume()) {
                OpenWeatherForecastResponse stale = weatherForecastCache.getStale(cacheKey);
                if (stale != null) {
                    response = stale; // fallback
                } else {
                    throw new ResponseStatusException(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "Daily OpenWeather API budget exhausted"
                    );
                }
            } else {
                // 2) Budget allowed → try OpenWeather call
                try {
                    response = openWeatherClient.fetchForecast(locationId, openWeatherUnit);
                    if (response != null) {
                        weatherForecastCache.put(cacheKey, response, 1800); // 30 min TTL
                    }
                } catch (RestClientResponseException ex) {
                    // Try stale cache fallback
                    OpenWeatherForecastResponse stale = weatherForecastCache.getStale(cacheKey);
                    if (stale != null) {
                        response = stale;
                    } else if (ex.getStatusCode().value() == 404) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid locationId");
                    } else {
                        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Weather provider unavailable");
                    }
                } catch (Exception ex) {
                    OpenWeatherForecastResponse stale = weatherForecastCache.getStale(cacheKey);
                    if (stale != null) {
                        response = stale;
                    } else {
                        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Weather provider unavailable");
                    }
                }
            }
        }



        if (response == null || response.list() == null || response.list().isEmpty()) {
            return List.of();
        }

        LocalDate tomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1);
        LocalDate lastDay = tomorrow.plusDays(4); // total 5 days

        Map<LocalDate, Double> dailyMax = response.list().stream()
                .collect(Collectors.toMap(
                        item -> Instant.ofEpochSecond(item.dt())
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate(),
                        item -> item.main().temp(),
                        Math::max
                ));


        return dailyMax.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(tomorrow) && !entry.getKey().isAfter(lastDay))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DailyTemperatureDto(entry.getKey().toString(), entry.getValue()))
                .toList();


    }

}

