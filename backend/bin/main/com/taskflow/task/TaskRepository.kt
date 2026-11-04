package com.taskflow.task

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TaskRepository : JpaRepository<Task, Long> {

    /** A null filter is ignored. The left join keeps tasks that have no assignee. */
    @Query(
        """
        select t from Task t
        left join t.assignee a
        where t.project.id = :projectId
          and (:status is null or t.status = :status)
          and (:priority is null or t.priority = :priority)
          and (:assigneeId is null or a.id = :assigneeId)
        """,
    )
    fun search(
        @Param("projectId") projectId: Long,
        @Param("status") status: TaskStatus?,
        @Param("priority") priority: Priority?,
        @Param("assigneeId") assigneeId: Long?,
        pageable: Pageable,
    ): Page<Task>

    fun findAllByProjectId(projectId: Long): List<Task>
}
