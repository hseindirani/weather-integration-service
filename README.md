# Weather Integration Service

Backend integration layer between a mobile app and the OpenWeatherMap API.
The service supports high traffic while staying within third-party API limits.

## Endpoints

### 1) Weather summary
Returns the user’s favorite locations where **tomorrow’s max temperature** is above a threshold.

`GET /weather/summary?unit=celsius&temperature=24&locations=2643743,5128581`

### 2) 5-day forecast for a location
Returns the **daily maximum temperature** for the next 5 days (starting tomorrow).

`GET /weather/locations/{locationId}`  
Example: `GET /weather/locations/2643743`

## Assumptions
- OpenWeatherMap returns multiple forecast points per day (3-hour intervals).
- “Tomorrow’s temperature” is interpreted as **tomorrow’s maximum temperature**.
- For the 5-day endpoint, the service returns **daily maximum temperatures**.

## Rate limit handling (10,000 calls/day)
To reduce calls to OpenWeatherMap, the service uses:
- In-memory cache (TTL 30 minutes) per `(locationId + unit)`
- Daily API call budget (10,000/day)

If the budget is exhausted or OpenWeatherMap is unavailable:
- The service falls back to **stale cached data** if available
- Otherwise returns **503 Service Unavailable**

## Running the project
Requires an OpenWeatherMap API key.

### Set environment variable

**Windows CMD**
```bat
setx OPENWEATHER_API_KEY "your_key_here"
```
**Run**
mvn spring-boot:run

**Swagger UI**

http://localhost:8080/swagger-ui/index.html
