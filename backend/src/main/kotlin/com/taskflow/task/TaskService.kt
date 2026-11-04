package com.taskflow.task

import com.taskflow.common.PageResponse
import com.taskflow.error.ApiException
import com.taskflow.project.ProjectService
import com.taskflow.user.AppUser
import com.taskflow.user.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/** Task rules. Each method checks that the user owns the project before doing anything. */
@Service
@Transactional
class TaskService(
    private val tasks: TaskRepository,
    private val users: UserRepository,
    private val projects: ProjectService,
) {
    @Transactional(readOnly = true)
    fun list(
        userId: Long,
        projectId: Long,
        filter: TaskFilter,
        sortKey: String?,
        page: Int,
        size: Int,
    ): PageResponse<TaskResponse> {
        projects.requireOwned(projectId, userId)
        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            TaskSortStrategies.resolve(sortKey),
        )
        val result = tasks.search(projectId, filter.status, filter.priority, filter.assigneeId, pageable)
        return PageResponse.from(result) { TaskMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun summary(userId: Long, projectId: Long): TaskSummaryResponse {
        projects.requireOwned(projectId, userId)
        return TaskMapper.summarize(tasks.findAllByProjectId(projectId))
    }

    fun create(userId: Long, projectId: Long, request: CreateTaskRequest): TaskResponse {
        val project = projects.requireOwned(projectId, userId)
        val task = Task(
            project = project,
            title = request.title.trim(),
            description = request.description.clean(),
            status = TaskStatus.TODO,
            priority = request.priority,
            dueDate = request.dueDate,
            assignee = resolveAssignee(request.assigneeId),
        )
        return TaskMapper.toResponse(tasks.save(task))
    }

    fun update(userId: Long, taskId: Long, request: UpdateTaskRequest): TaskResponse {
        val task = requireOwnedTask(taskId, userId)
        task.title = request.title.trim()
        task.description = request.description.clean()
        task.status = request.status
        task.priority = request.priority
        task.dueDate = request.dueDate
        task.assignee = resolveAssignee(request.assigneeId)
        task.updatedAt = Instant.now()
        return TaskMapper.toResponse(task)
    }

    fun updateStatus(userId: Long, taskId: Long, status: TaskStatus): TaskResponse {
        val task = requireOwnedTask(taskId, userId)
        task.status = status
        task.updatedAt = Instant.now()
        return TaskMapper.toResponse(task)
    }

    fun delete(userId: Long, taskId: Long) {
        tasks.delete(requireOwnedTask(taskId, userId))
    }

    /** 404 if the task doesn't exist, 403 if it belongs to another user's project. */
    private fun requireOwnedTask(taskId: Long, userId: Long): Task {
        val task = tasks.findById(taskId).orElse(null)
            ?: throw ApiException(HttpStatus.NOT_FOUND, "Task not found")
        if (task.project.owner.id != userId) {
            throw ApiException(HttpStatus.FORBIDDEN, "You don't have access to this task")
        }
        return task
    }

    private fun resolveAssignee(assigneeId: Long?): AppUser? {
        if (assigneeId == null) return null
        return users.findById(assigneeId).orElseThrow {
            ApiException(
                HttpStatus.BAD_REQUEST,
                "Invalid assignee",
                mapOf("assigneeId" to "User does not exist"),
            )
        }
    }
}

private fun String?.clean(): String? = this?.trim()?.ifBlank { null }