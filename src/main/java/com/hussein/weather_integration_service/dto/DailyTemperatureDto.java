package com.hussein.weather_integration_service.dto;

public record DailyTemperatureDto(
        String date,
        double maxTemperature
) {}
