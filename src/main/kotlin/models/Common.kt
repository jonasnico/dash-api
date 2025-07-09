package com.dash.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val data: T,
    val status: String,
    val message: String? = null
)
