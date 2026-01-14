package com.hussein.weather_integration_service.dto;

public record WeatherSummaryMatchDto(
        String locationId,
        String tomorrowDate,
        double tomorrowMaxTemp
) {}
