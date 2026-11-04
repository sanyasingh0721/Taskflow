package com.taskflow.config

import com.taskflow.user.AppUser
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService(
    @Value("\${taskflow.jwt.secret}") secret: String,
    @Value("\${taskflow.jwt.expiration-minutes}") private val expirationMinutes: Long,
) {
    private val signingKey = Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))

    fun generateToken(user: AppUser): String {
        val now = Date()
        return Jwts.builder()
            .subject(user.id.toString())
            .claim("email", user.email)
            .issuedAt(now)
            .expiration(Date(now.time + expirationMinutes * 60_000))
            .signWith(signingKey)
            .compact()
    }

    /** Returns the user id if the token is valid and unexpired, otherwise null. */
    fun extractUserId(token: String): Long? = try {
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
            .toLong()
    } catch (ex: JwtException) {
        null
    } catch (ex: IllegalArgumentException) {
        null
    }
}
