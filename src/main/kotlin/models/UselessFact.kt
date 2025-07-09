package com.dash.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UselessFact(
    val id: String,
    val text: String,
    val source: String,
    @SerialName("source_url") val sourceUrl: String,
    val language: String,
    val permalink: String
)
