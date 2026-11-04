package com.taskflow.error

import java.time.Instant

/** The single error shape every endpoint returns. */
data class ErrorResponse(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String,
    val fieldErrors: Map<String, String> = emptyMap(),
)
