package com.dash.services

import com.dash.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

class DashboardService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    // Simple cache with 5-minute TTL
    private data class CacheEntry<T>(val data: T, val timestamp: Long) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > 300_000 // 5 minutes
    }

    private val cache = mutableMapOf<String, CacheEntry<*>>()

    suspend fun getUselessFact(): UselessFact {
        return getFromCacheOrFetch("fact") {
            client.get("https://uselessfacts.jsph.pl/api/v2/facts/random").body<UselessFact>()
        }
    }

    suspend fun getOsloWeather(): CurrentWeather {
        val weatherData = getFromCacheOrFetch("weather") {
            client.get("https://api.met.no/weatherapi/locationforecast/2.0/compact") {
                parameter("lat", 59.9139)
                parameter("lon", 10.7522)
                header("User-Agent", "dash-api/1.0")
            }.body<WeatherData>()
        }
        return processWeatherData(weatherData)
    }

    suspend fun getGitHubActivity(username: String = "jonasnico"): List<GitHubEvent> {
        val events = getFromCacheOrFetch("github_$username") {
            client.get("https://api.github.com/users/$username/events/public") {
                header("User-Agent", "dash-api/1.0")
            }.body<List<GitHubEvent>>()
        }
        return events.take(5)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> getFromCacheOrFetch(key: String, fetcher: suspend () -> T): T {
        synchronized(cache) {
            val cached = cache[key] as? CacheEntry<T>
            if (cached != null && !cached.isExpired()) {
                return cached.data
            }
        }

        val data = fetcher()
        synchronized(cache) {
            cache[key] = CacheEntry(data, System.currentTimeMillis())
        }
        return data
    }

    private fun processWeatherData(weatherData: WeatherData): CurrentWeather {
        val timeseries = weatherData.properties.timeseries
        if (timeseries.isEmpty()) throw IllegalStateException("No weather data available")

        val current = timeseries[0]
        val forecast = timeseries.find { it.data.next6Hours != null }

        return CurrentWeather(
            temperature = current.data.instant.details.airTemperature.roundToInt(),
            humidity = current.data.instant.details.relativeHumidity,
            windSpeed = current.data.instant.details.windSpeed,
            symbolCode = forecast?.data?.next6Hours?.summary?.symbolCode ?: "clearsky_day",
            precipitation = forecast?.data?.next6Hours?.details?.precipitationAmount ?: 0.0
        )
    }

    fun close() {
        client.close()
    }
}
