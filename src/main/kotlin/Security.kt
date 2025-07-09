package com.dash

import io.ktor.server.application.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureSecurity() {
    val isProduction = System.getenv("ENVIRONMENT") == "production"

    install(DefaultHeaders) {
        // security headers
        header("X-Content-Type-Options", "nosniff")
        header("X-Frame-Options", "DENY")
        header("X-XSS-Protection", "1; mode=block")
        header("Referrer-Policy", "strict-origin-when-cross-origin")

        // HSTS for prod, investigate further
        if (isProduction) {
            header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
        }
    }
}
