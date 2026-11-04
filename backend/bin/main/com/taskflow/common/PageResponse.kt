package com.taskflow.common

import org.springframework.data.domain.Page

/** Our own paging format, so the JSON doesn't depend on Spring's internal page class. */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    companion object {
        fun <T, R> from(page: Page<T>, transform: (T) -> R): PageResponse<R> = PageResponse(
            content = page.content.map(transform),
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )
    }
}
