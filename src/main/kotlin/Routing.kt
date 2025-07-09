package com.dash

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.dash.services.DashboardService
import com.dash.models.ApiResponse

fun Application.configureRouting() {
    val dashboardService = DashboardService()

    routing {
        get("/") {
            call.respondText("Dash API - Ready")
        }

        route("/api") {
            get("/health") {
                call.respond(ApiResponse(
                    data = mapOf("status" to "healthy"),
                    status = "success"
                ))
            }

            get("/fact") {
                try {
                    val result = dashboardService.getUselessFact()
                    call.respond(ApiResponse(data = result, status = "success"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse(data = null, status = "error", message = e.message)
                    )
                }
            }

            get("/weather") {
                try {
                    val result = dashboardService.getOsloWeather()
                    call.respond(ApiResponse(data = result, status = "success"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse(data = null, status = "error", message = e.message)
                    )
                }
            }

            get("/github") {
                try {
                    val result = dashboardService.getGitHubActivity()
                    call.respond(ApiResponse(data = result, status = "success"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse(data = null, status = "error", message = e.message)
                    )
                }
            }

            get("/github/{username}") {
                try {
                    val username = call.parameters["username"] ?: "jonasnico"
                    val result = dashboardService.getGitHubActivity(username)
                    call.respond(ApiResponse(data = result, status = "success"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse(data = null, status = "error", message = e.message)
                    )
                }
            }
        }
    }

    // cleanup after stopping
    monitor.subscribe(ApplicationStopping) {
        dashboardService.close()
    }
}
