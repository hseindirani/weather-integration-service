package com.hussein.weather_integration_service.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.hussein.weather_integration_service.dto.openweather.OpenWeatherForecastResponse;

import org.springframework.web.util.UriBuilder;


@Component
public class OpenWeatherClient {

    private final RestClient restClient;
    private final String apiKey;

    public OpenWeatherClient(
            @Value("${openweather.baseUrl}") String baseUrl,
            @Value("${openweather.apiKey}") String apiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
    }

    public String fetchForecastRaw(long locationId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("id", locationId)
                        .queryParam("appid", apiKey)
                        .build())
                .retrieve()
                .body(String.class);
    }
    public OpenWeatherForecastResponse fetchForecast(long locationId, String unit) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("id", locationId)
                        .queryParam("units", unit)
                        .queryParam("appid", apiKey)
                        .build())
                .retrieve()
                .body(OpenWeatherForecastResponse.class);
    }

}
