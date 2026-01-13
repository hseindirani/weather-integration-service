package com.hussein.weather_integration_service.services;

import com.hussein.weather_integration_service.clients.OpenWeatherClient;
import com.hussein.weather_integration_service.dto.openweather.OpenWeatherForecastResponse;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;


import java.util.List;

@Service
public class WeatherSummaryService {
    private final OpenWeatherClient openWeatherClient;

    public WeatherSummaryService(OpenWeatherClient openWeatherClient) {
        this.openWeatherClient = openWeatherClient;
    }


    public List<String> getMatchingLocations(String unit, int temperatureThreshold, List<Long> locationIds) {

        String openWeatherUnit = unit.equalsIgnoreCase("celsius") ? "metric" : "imperial";

        return locationIds.stream()
                .filter(locationId -> {
                    OpenWeatherForecastResponse response =
                            openWeatherClient.fetchForecast(locationId, openWeatherUnit);

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
