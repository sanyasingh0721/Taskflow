package com.taskflow.task

import com.taskflow.user.UserSummary
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate

data class CreateTaskRequest(
    @field:NotBlank @field:Size(max = 120) val title: String,
    @field:Size(max = 2000) val description: String? = null,
    val priority: Priority = Priority.MEDIUM,
    @field:FutureOrPresent val dueDate: LocalDate? = null,   // rule: no past due dates on create
    val assigneeId: Long? = null,
)

data class UpdateTaskRequest(
    @field:NotBlank @field:Size(max = 120) val title: String,
    @field:Size(max = 2000) val description: String? = null,
    val status: TaskStatus,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val assigneeId: Long? = null,
)

data class StatusUpdateRequest(val status: TaskStatus)

data class TaskFilter(
    val status: TaskStatus? = null,
    val priority: Priority? = null,
    val assigneeId: Long? = null,
)

data class TaskResponse(
    val id: Long,
    val projectId: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: Priority,
    val dueDate: LocalDate?,
    val assignee: UserSummary?,
    val overdue: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class TaskSummaryResponse(
    val total: Int,
    val byStatus: Map<TaskStatus, Int>,
    val overdue: Int,
)
