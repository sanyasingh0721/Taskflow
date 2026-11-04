package com.taskflow.project

import com.taskflow.error.ApiException
import com.taskflow.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectService(
    private val projects: ProjectRepository,
    private val users: UserRepository,
) {
    @Transactional(readOnly = true)
    fun listForUser(userId: Long): List<ProjectResponse> =
        projects.findAllByOwnerIdOrderByCreatedAtDesc(userId).map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getOwned(userId: Long, projectId: Long): ProjectResponse =
        requireOwned(projectId, userId).toResponse()

    @Transactional
    fun create(userId: Long, request: CreateProjectRequest): ProjectResponse {
        val owner = users.findById(userId)
            .orElseThrow { ApiException(HttpStatus.UNAUTHORIZED, "User no longer exists") }
        val project = projects.save(
            Project(
                name = request.name.trim(),
                description = request.description?.trim()?.ifBlank { null },
                owner = owner,
            ),
        )
        return project.toResponse()
    }

    /** 404 if the project doesn't exist, 403 if another user owns it. */
    @Transactional(readOnly = true)
    fun requireOwned(projectId: Long, userId: Long): Project {
        val project = projects.findById(projectId).orElse(null)
            ?: throw ApiException(HttpStatus.NOT_FOUND, "Project not found")
        if (project.owner.id != userId) {
            throw ApiException(HttpStatus.FORBIDDEN, "You don't have access to this project")
        }
        return project
    }
}