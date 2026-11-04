package com.taskflow.task

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDate

/** End-to-end test through the real security filter, controllers, services and H2 database. */
@SpringBootTest
@AutoConfigureMockMvc
class TaskflowApiIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `user can register, create a project and task, and read them back`() {
        val registerResult = mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Integration User","email":"integration@test.dev","password":"password123"}"""
        }.andExpect {
            status { isCreated() }
        }.andReturn()
        val token: String = JsonPath.read(registerResult.response.contentAsString, "$.token")
        val authHeader = "Bearer $token"

        val projectResult = mockMvc.post("/api/projects") {
            contentType = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, authHeader)
            content = """{"name":"Test Project","description":null}"""
        }.andExpect {
            status { isCreated() }
        }.andReturn()
        val projectId = JsonPath.read<Number>(projectResult.response.contentAsString, "$.id").toLong()

        mockMvc.post("/api/projects/$projectId/tasks") {
            contentType = MediaType.APPLICATION_JSON
            header(HttpHeaders.AUTHORIZATION, authHeader)
            content = """{"title":"First task","priority":"HIGH","dueDate":"${LocalDate.now().plusDays(2)}"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("First task") }
        }

        mockMvc.get("/api/projects/$projectId/tasks?status=TODO") {
            header(HttpHeaders.AUTHORIZATION, authHeader)
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(1) }
        }
    }

    @Test
    fun `requests without a token are rejected with 401`() {
        mockMvc.get("/api/projects").andExpect {
            status { isUnauthorized() }
        }
    }
}
