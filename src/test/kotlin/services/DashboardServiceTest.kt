package com.dash.services

import com.dash.models.*
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.*

class DashboardServiceTest {

    private fun createMockClient(responses: Map<String, String>): HttpClient {
        return HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler { request ->
                    val url = request.url.toString()
                    val response = when {
                        url.contains("uselessfacts") -> responses["fact"] ?: mockFactResponse
                        url.contains("api.met.no") -> responses["weather"] ?: mockWeatherResponse
                        url.contains("github.com") -> responses["github"] ?: mockGitHubResponse
                        else -> "{}"
                    }
                    respond(
                        content = response,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        }
    }

    @Test
    fun `test getUselessFact returns fact`() = runTest {
        val mockClient = createMockClient(emptyMap())
        val service = DashboardService()

        // Use reflection to replace the client for testing
        val clientField = service::class.java.getDeclaredField("client")
        clientField.isAccessible = true
        clientField.set(service, mockClient)

        val fact = service.getUselessFact()

        assertNotNull(fact)
        assertEquals("test-id", fact.id)
        assertEquals("This is a test fact", fact.text)
        assertEquals("test", fact.source)

        service.close()
        mockClient.close()
    }

    @Test
    fun `test getOsloWeather returns current weather`() = runTest {
        val mockClient = createMockClient(emptyMap())
        val service = DashboardService()

        val clientField = service::class.java.getDeclaredField("client")
        clientField.isAccessible = true
        clientField.set(service, mockClient)

        val weather = service.getOsloWeather()

        assertNotNull(weather)
        assertEquals(15, weather.temperature)
        assertEquals(65.0, weather.humidity)
        assertEquals(5.2, weather.windSpeed)

        service.close()
        mockClient.close()
    }

    @Test
    fun `test getGitHubActivity returns events`() = runTest {
        val mockClient = createMockClient(emptyMap())
        val service = DashboardService()

        val clientField = service::class.java.getDeclaredField("client")
        clientField.isAccessible = true
        clientField.set(service, mockClient)

        val events = service.getGitHubActivity("testuser")

        assertNotNull(events)
        assertTrue(events.isNotEmpty())
        assertEquals("PushEvent", events[0].type)
        assertEquals("testuser/testrepo", events[0].repo.name)

        service.close()
        mockClient.close()
    }

    @Test
    fun `test getGitHubActivity limits to 5 events`() = runTest {
        val mockClient = createMockClient(mapOf("github" to mockGitHubResponseMany))
        val service = DashboardService()

        val clientField = service::class.java.getDeclaredField("client")
        clientField.isAccessible = true
        clientField.set(service, mockClient)

        val events = service.getGitHubActivity()

        assertEquals(5, events.size)

        service.close()
        mockClient.close()
    }

    companion object {
        private const val mockFactResponse = """
        {
            "id": "test-id",
            "text": "This is a test fact",
            "source": "test",
            "source_url": "https://test.com",
            "language": "en",
            "permalink": "https://test.com/fact"
        }
        """

        private const val mockWeatherResponse = """
        {
            "properties": {
                "timeseries": [
                    {
                        "time": "2025-07-09T12:00:00Z",
                        "data": {
                            "instant": {
                                "details": {
                                    "air_temperature": 15.0,
                                    "relative_humidity": 65.0,
                                    "wind_speed": 5.2
                                }
                            },
                            "next_6_hours": {
                                "summary": {
                                    "symbol_code": "partlycloudy_day"
                                },
                                "details": {
                                    "precipitation_amount": 0.0
                                }
                            }
                        }
                    }
                ]
            }
        }
        """

        private const val mockGitHubResponse = """
        [
            {
                "id": "123",
                "type": "PushEvent",
                "repo": {
                    "name": "testuser/testrepo",
                    "url": "https://github.com/testuser/testrepo"
                },
                "payload": {
                    "commits": [
                        {"message": "Test commit"}
                    ]
                },
                "created_at": "2025-07-09T12:00:00Z"
            }
        ]
        """

        private val mockGitHubResponseMany = """
        [
            ${(1..10).joinToString(",") { i ->
                """
                {
                    "id": "$i",
                    "type": "PushEvent",
                    "repo": {
                        "name": "testuser/testrepo$i",
                        "url": "https://github.com/testuser/testrepo$i"
                    },
                    "payload": {
                        "commits": [
                            {"message": "Test commit $i"}
                        ]
                    },
                    "created_at": "2025-07-09T12:00:00Z"
                }
                """.trimIndent()
            }}
        ]
        """
    }
}
