package com.hussein.weather_integration_service.services;

import com.hussein.weather_integration_service.dto.DailyTemperatureDto;

import java.util.List;

public interface WeatherLocationService {
    List<DailyTemperatureDto> getNext5DaysMaxTemps(long locationId, String unit);
}
