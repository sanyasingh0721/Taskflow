package com.taskflow.config

import org.springframework.security.core.Authentication

/** The JWT filter stores the user id as the principal. This reads it back in controllers. */
fun Authentication.userId(): Long = principal as Long
