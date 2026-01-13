# Weather Integration Service

This project is a backend integration layer between a mobile app and the OpenWeatherMap API.
Its purpose is to serve weather data at scale while staying within third-party API rate limits.

## Features

### 1) Weather summary
Returns the user’s favorite locations where **tomorrow’s temperature** is above a given threshold.

`GET /weather/summary?unit=celsius&temperature=24&locations=2643743,5128581`

### 2) 5-day forecast for a location
Returns the **daily maximum temperature** for the next 5 days for one location.

`GET /weather/locations/{locationId}`

Example:
`GET /weather/locations/2643743`

## Assumptions
- OpenWeatherMap returns multiple forecast points per day (3-hour intervals).
- For summary comparisons, **tomorrow’s temperature** is interpreted as **tomorrow's maximum temperature**.
- For the 5-day endpoint, the service returns the **daily maximum temperature** for the next 5 days (starting tomorrow).

## Rate limit protection (Caching)
To stay within the OpenWeatherMap request limit, the service uses an **in-memory cache with TTL (30 minutes)**.
Forecast data is cached per `(locationId + unit)` and reused across requests.

In a production environment, this cache could be replaced with a distributed cache such as Redis.

## Running the project
This project requires an OpenWeatherMap API key.

Set the environment variable:

```bash
OPENWEATHER_API_KEY=your_key_here
