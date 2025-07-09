package com.dash

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureHTTP() {
    val isDevelopment = System.getenv("ENVIRONMENT") != "production"

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)

        // deployed dash url
        allowHost("dash-nine-pi.vercel.app", schemes = listOf("https"))

        // Development URLs
        if (isDevelopment) {
            allowHost("localhost:5173") // Vite dev
            allowHost("localhost:3000") // Alt local dev
        }

        // Additional hosts from environment
        System.getenv("ALLOWED_HOSTS")?.split(",")?.forEach { host ->
            val trimmedHost = host.trim()
            if (trimmedHost.isNotEmpty()) {
                allowHost(trimmedHost, schemes = listOf("https"))
            }
        }
    }
}
