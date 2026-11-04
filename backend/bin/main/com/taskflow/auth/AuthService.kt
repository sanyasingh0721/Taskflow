package com.taskflow.auth

import com.taskflow.config.JwtService
import com.taskflow.error.ApiException
import com.taskflow.user.AppUser
import com.taskflow.user.UserRepository
import com.taskflow.user.toSummary
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val users: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        if (users.existsByEmail(email)) {
            throw ApiException(
                HttpStatus.CONFLICT,
                "Email is already registered",
                mapOf("email" to "This email is already registered"),
            )
        }
        val user = users.save(
            AppUser(
                name = request.name.trim(),
                email = email,
                passwordHash = passwordEncoder.encode(request.password),
            ),
        )
        return AuthResponse(jwtService.generateToken(user), user.toSummary())
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        // Same message for an unknown email and a wrong password, so nobody can guess which emails exist.
        val user = users.findByEmail(request.email.trim().lowercase())
            ?.takeIf { passwordEncoder.matches(request.password, it.passwordHash) }
            ?: throw ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
        return AuthResponse(jwtService.generateToken(user), user.toSummary())
    }
}
