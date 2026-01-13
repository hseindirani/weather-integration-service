package com.hussein.weather_integration_service.cache;

import com.hussein.weather_integration_service.dto.openweather.OpenWeatherForecastResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WeatherForecastCache {

    private static class CacheEntry {
        private final OpenWeatherForecastResponse response;
        private final Instant expiresAt;

        private CacheEntry(OpenWeatherForecastResponse response, Instant expiresAt) {
            this.response = response;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public OpenWeatherForecastResponse get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;

        if (Instant.now().isAfter(entry.expiresAt)) {
            cache.remove(key);
            return null;
        }
        return entry.response;
    }

    public void put(String key, OpenWeatherForecastResponse response, int ttlSeconds) {
        Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);
        cache.put(key, new CacheEntry(response, expiresAt));
    }
}
