package com.taskflow.task

import com.taskflow.common.PageResponse
import com.taskflow.config.userId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TaskController(private val taskService: TaskService) {

    @GetMapping("/api/projects/{projectId}/tasks")
    fun list(
        authentication: Authentication,
        @PathVariable("projectId") projectId: Long,
        @RequestParam("status", required = false) status: TaskStatus?,
        @RequestParam("priority", required = false) priority: Priority?,
        @RequestParam("assigneeId", required = false) assigneeId: Long?,
        @RequestParam("sort", required = false) sort: String?,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "20") size: Int,
    ): PageResponse<TaskResponse> = taskService.list(
        authentication.userId(),
        projectId,
        TaskFilter(status, priority, assigneeId),
        sort,
        page,
        size,
    )

    @GetMapping("/api/projects/{projectId}/summary")
    fun summary(
        authentication: Authentication,
        @PathVariable("projectId") projectId: Long,
    ): TaskSummaryResponse = taskService.summary(authentication.userId(), projectId)

    @PostMapping("/api/projects/{projectId}/tasks")
    fun create(
        authentication: Authentication,
        @PathVariable("projectId") projectId: Long,
        @Valid @RequestBody request: CreateTaskRequest,
    ): ResponseEntity<TaskResponse> = ResponseEntity.status(HttpStatus.CREATED)
        .body(taskService.create(authentication.userId(), projectId, request))

    @PutMapping("/api/tasks/{taskId}")
    fun update(
        authentication: Authentication,
        @PathVariable("taskId") taskId: Long,
        @Valid @RequestBody request: UpdateTaskRequest,
    ): TaskResponse = taskService.update(authentication.userId(), taskId, request)

    @PatchMapping("/api/tasks/{taskId}/status")
    fun updateStatus(
        authentication: Authentication,
        @PathVariable("taskId") taskId: Long,
        @RequestBody request: StatusUpdateRequest,
    ): TaskResponse = taskService.updateStatus(authentication.userId(), taskId, request.status)

    @DeleteMapping("/api/tasks/{taskId}")
    fun delete(
        authentication: Authentication,
        @PathVariable("taskId") taskId: Long,
    ): ResponseEntity<Void> {
        taskService.delete(authentication.userId(), taskId)
        return ResponseEntity.noContent().build()
    }
}
