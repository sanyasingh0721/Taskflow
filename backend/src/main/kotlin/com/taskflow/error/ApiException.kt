package com.taskflow.error

import org.springframework.http.HttpStatus

/** Business error with an HTTP status. fieldErrors lets a rule point at a specific input. */
class ApiException(
    val status: HttpStatus,
    message: String,
    val fieldErrors: Map<String, String> = emptyMap(),
) : RuntimeException(message)
