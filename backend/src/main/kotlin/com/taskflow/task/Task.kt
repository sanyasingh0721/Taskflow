package com.taskflow.task

import com.taskflow.project.Project
import com.taskflow.user.AppUser
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.Instant
import java.time.LocalDate

enum class TaskStatus { TODO, IN_PROGRESS, DONE }

/**
 * Stored by position so sorting works (LOW, MEDIUM, HIGH). Don't reorder these once there is data.
 */
enum class Priority { LOW, MEDIUM, HIGH }

@Entity
class Task(
    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    var project: Project,
    var title: String,
    var description: String?,
    @Enumerated(EnumType.STRING)
    var status: TaskStatus = TaskStatus.TODO,
    @Enumerated(EnumType.ORDINAL)
    var priority: Priority = Priority.MEDIUM,
    var dueDate: LocalDate?,
    @ManyToOne
    @JoinColumn(name = "assignee_id")
    var assignee: AppUser?,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
