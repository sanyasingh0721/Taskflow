package com.taskflow.project

import com.taskflow.config.userId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/projects")
class ProjectController(private val projectService: ProjectService) {

    @GetMapping
    fun list(authentication: Authentication): List<ProjectResponse> =
        projectService.listForUser(authentication.userId())

    @GetMapping("/{id}")
    fun get(authentication: Authentication, @PathVariable("id") id: Long): ProjectResponse =
        projectService.getOwned(authentication.userId(), id)

    @PostMapping
    fun create(
        authentication: Authentication,
        @Valid @RequestBody request: CreateProjectRequest,
    ): ResponseEntity<ProjectResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(authentication.userId(), request))
}
