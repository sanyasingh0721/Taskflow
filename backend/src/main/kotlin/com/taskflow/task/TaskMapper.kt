package com.taskflow.task

import com.taskflow.user.toSummary
import java.time.LocalDate

/**
 * Converts entities into response objects. Keeping this in one place means the API
 * shape can change without touching the database model.
 */
object TaskMapper {

    fun toResponse(task: Task, today: LocalDate = LocalDate.now()): TaskResponse = TaskResponse(
        id = task.id!!,
        projectId = task.project.id!!,
        title = task.title,
        description = task.description,
        status = task.status,
        priority = task.priority,
        dueDate = task.dueDate,
        assignee = task.assignee?.toSummary(),
        overdue = isOverdue(task, today),
        createdAt = task.createdAt,
        updatedAt = task.updatedAt,
    )

    fun summarize(tasks: List<Task>, today: LocalDate = LocalDate.now()): TaskSummaryResponse = TaskSummaryResponse(
        total = tasks.size,
        byStatus = TaskStatus.entries.associateWith { status -> tasks.count { it.status == status } },
        overdue = tasks.count { isOverdue(it, today) },
    )

    /** Overdue means the due date has passed and the task isn't done yet. */
    fun isOverdue(task: Task, today: LocalDate): Boolean {
        val due = task.dueDate ?: return false
        return task.status != TaskStatus.DONE && due.isBefore(today)
    }
}
