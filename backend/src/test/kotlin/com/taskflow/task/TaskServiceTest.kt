package com.taskflow.task

import com.taskflow.error.ApiException
import com.taskflow.project.Project
import com.taskflow.project.ProjectService
import com.taskflow.user.AppUser
import com.taskflow.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.util.Optional

/** Unit tests for the service layer: dependencies are mocked with MockK. */
class TaskServiceTest {

    private val tasks = mockk<TaskRepository>()
    private val users = mockk<UserRepository>()
    private val projects = mockk<ProjectService>()
    private val service = TaskService(tasks, users, projects)

    private val owner = AppUser(name = "Ann", email = "ann@example.com", passwordHash = "hash").apply { id = 1L }
    private val project = Project(name = "Launch", description = null, owner = owner).apply { id = 10L }

    private fun taskWithId(id: Long) = Task(
        project = project,
        title = "Write docs",
        description = null,
        dueDate = null,
        assignee = null,
    ).apply { this.id = id }

    @Test
    fun `owner can change the status of their task`() {
        every { tasks.findById(5L) } returns Optional.of(taskWithId(5L))

        val result = service.updateStatus(userId = 1L, taskId = 5L, status = TaskStatus.DONE)

        assertEquals(TaskStatus.DONE, result.status)
    }

    @Test
    fun `a user cannot change a task that belongs to someone else`() {
        every { tasks.findById(5L) } returns Optional.of(taskWithId(5L))

        val ex = assertThrows<ApiException> {
            service.updateStatus(userId = 2L, taskId = 5L, status = TaskStatus.DONE)
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.status)
    }

    @Test
    fun `creating a task in a project the user does not own fails`() {
        every { projects.requireOwned(10L, 2L) } throws ApiException(HttpStatus.NOT_FOUND, "Project not found")

        assertThrows<ApiException> {
            service.create(
                userId = 2L,
                projectId = 10L,
                request = CreateTaskRequest(title = "Sneaky task"),
            )
        }
    }
}
