package com.hussein.weather_integration_service.controllers;

import com.hussein.weather_integration_service.clients.OpenWeatherClient;
import com.hussein.weather_integration_service.dto.WeatherSummaryResponseDto;
import com.hussein.weather_integration_service.services.WeatherSummaryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/weather")

public class WeatherSummaryController {
    private final WeatherSummaryService weatherSummaryService;


    public WeatherSummaryController(
            WeatherSummaryService weatherSummaryService) {
        this.weatherSummaryService = weatherSummaryService;

    }

    @GetMapping("/summary")
    public WeatherSummaryResponseDto summary(
            @RequestParam String unit,
            @RequestParam int temperature,
            @RequestParam String locations)
    {
        if (!isValidUnit(unit)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid unit. Allowed values: celsius or fahrenheit"
            );
        }

        if (locations == null || locations.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "locations must be provided as comma-separated location ids"
            );
        }
        List<Long> locationIds;

        try {
            locationIds = Stream.of(locations.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "locations must contain only numeric ids"
            );
        }

        List<String> matches = weatherSummaryService.getMatchingLocations(unit, temperature, locationIds);
        return new WeatherSummaryResponseDto(unit, temperature, matches);

    }
    private static boolean isValidUnit(String unit) {
        return "celsius".equalsIgnoreCase(unit) || "fahrenheit".equalsIgnoreCase(unit);
    }

}
