package com.dash.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val properties: WeatherProperties
)

@Serializable
data class WeatherProperties(
    val timeseries: List<WeatherTimeSeries>
)

@Serializable
data class WeatherTimeSeries(
    val time: String,
    val data: WeatherTimeSeriesData
)

@Serializable
data class WeatherTimeSeriesData(
    val instant: WeatherInstant,
    @SerialName("next_6_hours") val next6Hours: WeatherNext6Hours? = null
)

@Serializable
data class WeatherInstant(
    val details: WeatherDetails
)

@Serializable
data class WeatherDetails(
    @SerialName("air_temperature") val airTemperature: Double,
    @SerialName("relative_humidity") val relativeHumidity: Double,
    @SerialName("wind_speed") val windSpeed: Double
)

@Serializable
data class WeatherNext6Hours(
    val summary: WeatherSummary,
    val details: WeatherPrecipitationDetails
)

@Serializable
data class WeatherSummary(
    @SerialName("symbol_code") val symbolCode: String
)

@Serializable
data class WeatherPrecipitationDetails(
    @SerialName("precipitation_amount") val precipitationAmount: Double
)

@Serializable
data class CurrentWeather(
    val temperature: Int,
    val humidity: Double,
    val windSpeed: Double,
    val symbolCode: String,
    val precipitation: Double
)
