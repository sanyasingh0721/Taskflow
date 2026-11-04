package com.taskflow.project

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreateProjectRequest(
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:Size(max = 500) val description: String? = null,
)

data class ProjectResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Instant,
)

fun Project.toResponse(): ProjectResponse = ProjectResponse(
    id = id!!,
    name = name,
    description = description,
    createdAt = createdAt,
)
