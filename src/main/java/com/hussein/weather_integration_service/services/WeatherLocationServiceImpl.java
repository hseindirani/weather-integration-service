package com.hussein.weather_integration_service.services;

import com.hussein.weather_integration_service.clients.OpenWeatherClient;
import com.hussein.weather_integration_service.cache.WeatherForecastCache;
import com.hussein.weather_integration_service.dto.DailyTemperatureDto;
import com.hussein.weather_integration_service.dto.openweather.OpenWeatherForecastResponse;
import org.springframework.stereotype.Service;

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
        // TODO: implement next
        return List.of();
    }
}
