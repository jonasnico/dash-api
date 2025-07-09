package com.dash

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.*

class ApplicationTest {
    @Test
    fun `test root endpoint returns success`() = testApplication {
        application {
            module()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Dash API - Ready", bodyAsText())
        }
    }

    @Test
    fun `test health endpoint returns healthy status`() = testApplication {
        application {
            module()
        }
        client.get("/api/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = Json.parseToJsonElement(bodyAsText()).jsonObject
            assertEquals("success", response["status"]?.jsonPrimitive?.content)
            assertTrue(response["data"]?.jsonObject?.containsKey("status") == true)
        }
    }

    @Test
    fun `test CORS headers are present`() = testApplication {
        application {
            module()
        }
        client.get("/api/health") {
            header(HttpHeaders.Origin, "https://dash-nine-pi.vercel.app")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            // CORS headers should be present when making requests from allowed origins
            assertNotNull(headers[HttpHeaders.AccessControlAllowOrigin])
        }
    }

    @Test
    fun `test security headers are present`() = testApplication {
        application {
            module()
        }
        client.get("/api/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("nosniff", headers["X-Content-Type-Options"])
            assertEquals("DENY", headers["X-Frame-Options"])
            assertEquals("1; mode=block", headers["X-XSS-Protection"])
            assertEquals("strict-origin-when-cross-origin", headers["Referrer-Policy"])
        }
    }

    @Test
    fun `test fact endpoint returns proper structure`() = testApplication {
        application {
            module()
        }
        client.get("/api/fact").apply {
            // Note: This will make a real API call in test environment
            // In production tests, you'd mock this
            val response = Json.parseToJsonElement(bodyAsText()).jsonObject
            assertTrue(response.containsKey("status"))
            assertTrue(response.containsKey("data"))
        }
    }

    @Test
    fun `test weather endpoint returns proper structure`() = testApplication {
        application {
            module()
        }
        client.get("/api/weather").apply {
            val response = Json.parseToJsonElement(bodyAsText()).jsonObject
            assertTrue(response.containsKey("status"))
            assertTrue(response.containsKey("data"))
        }
    }

    @Test
    fun `test github endpoint returns proper structure`() = testApplication {
        application {
            module()
        }
        client.get("/api/github").apply {
            val response = Json.parseToJsonElement(bodyAsText()).jsonObject
            assertTrue(response.containsKey("status"))
            assertTrue(response.containsKey("data"))
        }
    }

    @Test
    fun `test github endpoint with username parameter`() = testApplication {
        application {
            module()
        }
        client.get("/api/github/octocat").apply {
            val response = Json.parseToJsonElement(bodyAsText()).jsonObject
            assertTrue(response.containsKey("status"))
            assertTrue(response.containsKey("data"))
        }
    }

    @Test
    fun `test invalid endpoint returns 404`() = testApplication {
        application {
            module()
        }
        client.get("/api/nonexistent").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }
}
