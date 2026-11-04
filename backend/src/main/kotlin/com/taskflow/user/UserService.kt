package com.taskflow.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val users: UserRepository) {

    /** Used to fill the assignee dropdown. Any logged-in user can see the list. */
    @Transactional(readOnly = true)
    fun listAll(): List<UserSummary> = users.findAll().map { it.toSummary() }
}
