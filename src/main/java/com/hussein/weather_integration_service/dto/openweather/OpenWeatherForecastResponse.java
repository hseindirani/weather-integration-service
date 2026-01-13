package com.hussein.weather_integration_service.dto.openweather;

import java.util.List;

public record OpenWeatherForecastResponse(List<OpenWeatherForecastItem> list) {}
