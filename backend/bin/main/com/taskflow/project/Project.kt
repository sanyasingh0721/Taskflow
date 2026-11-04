package com.taskflow.project

import com.taskflow.user.AppUser
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.Instant

@Entity
class Project(
    var name: String,
    var description: String?,
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    var owner: AppUser,
    var createdAt: Instant = Instant.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
