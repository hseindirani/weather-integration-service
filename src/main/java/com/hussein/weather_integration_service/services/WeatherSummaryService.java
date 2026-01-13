package com.hussein.weather_integration_service.services;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeatherSummaryService {

    public List<String> getMatchingLocations(String unit, int temperatureThreshold, List<Long> locationIds) {
        // temporary: return all locations as “matching”
        return locationIds.stream()
                .map(String::valueOf)
                .toList();
    }
}
