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


import java.util.List;

@Service
public class WeatherLocationServiceImpl implements WeatherLocationService {

    private final OpenWeatherClient openWeatherClient;
    private final WeatherForecastCache weatherForecastCache;

    public WeatherLocationServiceImpl(OpenWeatherClient openWeatherClient, WeatherForecastCache weatherForecastCache) {
        this.openWeatherClient = openWeatherClient;
        this.weatherForecastCache = weatherForecastCache;
    }

    @Override
    public List<DailyTemperatureDto> getNext5DaysMaxTemps(long locationId, String unit) {

        String openWeatherUnit = unit.equalsIgnoreCase("celsius") ? "metric" : "imperial";
        String cacheKey = "forecast:" + locationId + ":" + openWeatherUnit;

        OpenWeatherForecastResponse response = weatherForecastCache.get(cacheKey);

        if (response == null) {
            response = openWeatherClient.fetchForecast(locationId, openWeatherUnit);
            if (response != null) {
                weatherForecastCache.put(cacheKey, response, 1800); // 30 min TTL
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

