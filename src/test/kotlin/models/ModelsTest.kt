package com.dash.models

import kotlinx.serialization.json.Json
import kotlin.test.*

class ModelsTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test UselessFact serialization`() {
        val fact = UselessFact(
            id = "test-id",
            text = "Test fact",
            source = "test",
            sourceUrl = "https://test.com",
            language = "en",
            permalink = "https://test.com/fact"
        )

        val jsonString = json.encodeToString(UselessFact.serializer(), fact)
        val decoded = json.decodeFromString(UselessFact.serializer(), jsonString)

        assertEquals(fact, decoded)
    }

    @Test
    fun `test CurrentWeather serialization`() {
        val weather = CurrentWeather(
            temperature = 20,
            humidity = 65.0,
            windSpeed = 5.5,
            symbolCode = "partlycloudy_day",
            precipitation = 0.0
        )

        val jsonString = json.encodeToString(CurrentWeather.serializer(), weather)
        val decoded = json.decodeFromString(CurrentWeather.serializer(), jsonString)

        assertEquals(weather, decoded)
    }

    @Test
    fun `test GitHubEvent serialization`() {
        val event = GitHubEvent(
            id = "123",
            type = "PushEvent",
            repo = GitHubRepo("user/repo", "https://github.com/user/repo"),
            payload = GitHubPayload(
                commits = listOf(GitHubCommit("Test commit"))
            ),
            createdAt = "2025-07-09T12:00:00Z"
        )

        val jsonString = json.encodeToString(GitHubEvent.serializer(), event)
        val decoded = json.decodeFromString(GitHubEvent.serializer(), jsonString)

        assertEquals(event, decoded)
    }

    @Test
    fun `test ApiResponse serialization with string data`() {
        val response = ApiResponse(
            data = "test value",
            status = "success",
            message = "All good"
        )

        // Test basic properties
        assertEquals("test value", response.data)
        assertEquals("success", response.status)
        assertEquals("All good", response.message)
    }

    @Test
    fun `test WeatherData deserialization from API response`() {
        val weatherJson = """
        {
            "properties": {
                "timeseries": [
                    {
                        "time": "2025-07-09T12:00:00Z",
                        "data": {
                            "instant": {
                                "details": {
                                    "air_temperature": 15.5,
                                    "relative_humidity": 70.0,
                                    "wind_speed": 3.2
                                }
                            },
                            "next_6_hours": {
                                "summary": {
                                    "symbol_code": "cloudy"
                                },
                                "details": {
                                    "precipitation_amount": 1.5
                                }
                            }
                        }
                    }
                ]
            }
        }
        """

        val weatherData = json.decodeFromString<WeatherData>(weatherJson)

        assertNotNull(weatherData)
        assertEquals(1, weatherData.properties.timeseries.size)
        assertEquals(15.5, weatherData.properties.timeseries[0].data.instant.details.airTemperature)
        assertEquals("cloudy", weatherData.properties.timeseries[0].data.next6Hours?.summary?.symbolCode)
    }
}
