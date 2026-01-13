package com.hussein.weather_integration_service.dto;

import java.util.List;

public record WeatherSummaryResponseDto(
        String unit,
        int temperatureThreshold,
        List<String> matchingLocations
) {}
