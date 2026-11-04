package com.taskflow.user

data class UserSummary(val id: Long, val name: String, val email: String)

fun AppUser.toSummary(): UserSummary = UserSummary(id = id!!, name = name, email = email)
