package com.hussein.weather_integration_service.controllers;

import com.hussein.weather_integration_service.dto.DailyTemperatureDto;
import com.hussein.weather_integration_service.dto.LocationForecastResponseDto;
import com.hussein.weather_integration_service.services.WeatherLocationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/weather")
public class WeatherLocationsController {
    private final WeatherLocationService weatherLocationService;

    public WeatherLocationsController(WeatherLocationService weatherLocationService) {
        this.weatherLocationService = weatherLocationService;
    }


    @GetMapping("/locations/{locationId}")
    public LocationForecastResponseDto locationForecast(
            @PathVariable String locationId,
            @RequestParam(defaultValue = "celsius") String unit
    ) {
        long id;
        try {
            id = Long.parseLong(locationId);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "locationId must be a numeric id"
            );
        }

        return new LocationForecastResponseDto(
                locationId,
                unit,
                weatherLocationService.getNext5DaysMaxTemps(id, unit)
        );
    }
}