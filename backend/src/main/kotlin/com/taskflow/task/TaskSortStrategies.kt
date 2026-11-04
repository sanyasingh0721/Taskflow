package com.taskflow.task

import com.taskflow.error.ApiException
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus

/**
 * Each sort option is its own class. To add a new one, write a class and register it below.
 */
interface TaskSortStrategy {
    val key: String
    fun toSort(): Sort
}

class DueDateSortStrategy : TaskSortStrategy {
    override val key = "dueDate"
    override fun toSort(): Sort = Sort.by(Sort.Order.asc("dueDate").nullsLast()).and(Sort.by("id"))
}

class PrioritySortStrategy : TaskSortStrategy {
    override val key = "priority"
    override fun toSort(): Sort = Sort.by(Sort.Order.desc("priority")).and(Sort.by("id"))
}

object TaskSortStrategies {
    private val strategies: Map<String, TaskSortStrategy> =
        listOf(DueDateSortStrategy(), PrioritySortStrategy()).associateBy { it.key }

    private val defaultSort: Sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))

    fun resolve(sortKey: String?): Sort {
        if (sortKey == null) return defaultSort
        val strategy = strategies[sortKey] ?: throw ApiException(
            HttpStatus.BAD_REQUEST,
            "Unsupported sort",
            mapOf("sort" to "Use one of: ${strategies.keys.joinToString()}"),
        )
        return strategy.toSort()
    }
}
