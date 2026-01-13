package com.hussein.weather_integration_service.dto;

import java.util.List;

public record LocationForecastResponseDto(
        String locationId,
        String unit,
        List<DailyTemperatureDto> days
) {}
