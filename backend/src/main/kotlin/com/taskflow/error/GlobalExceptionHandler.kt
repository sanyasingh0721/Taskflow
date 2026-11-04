package com.taskflow.error

import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.ErrorResponse as SpringErrorResponse
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * The single place where errors become HTTP responses. Controllers and services don't catch
 * errors themselves. They throw, and this class decides the status code and message.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ApiException::class)
    fun handleApi(ex: ApiException): ResponseEntity<ErrorResponse> =
        build(ex.status, ex.message ?: ex.status.reasonPhrase, ex.fieldErrors)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return build(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.BAD_REQUEST, "Malformed request body")

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> =
        build(HttpStatus.BAD_REQUEST, "Invalid value for parameter", mapOf(ex.name to "Invalid value"))

    @ExceptionHandler(DataAccessException::class)
    fun handleDatabase(ex: DataAccessException): ResponseEntity<ErrorResponse> {
        log.error("Database operation failed", ex)
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred. Please try again later.")
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> {
        // Spring's own errors (unknown URL, wrong HTTP method) already carry their status code.
        if (ex is SpringErrorResponse) {
            val status = HttpStatus.valueOf(ex.statusCode.value())
            return build(status, status.reasonPhrase)
        }
        log.error("Unexpected error", ex)
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.")
    }

    private fun build(
        status: HttpStatus,
        message: String,
        fieldErrors: Map<String, String> = emptyMap(),
    ): ResponseEntity<ErrorResponse> = ResponseEntity.status(status).body(
        ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = message,
            fieldErrors = fieldErrors,
        ),
    )
}