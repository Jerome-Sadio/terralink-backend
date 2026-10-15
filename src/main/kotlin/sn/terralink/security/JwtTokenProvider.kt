package sn.terralink.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.UUID

@Component
class JwtTokenProvider(
    @Value("\${terralink.jwt.secret}") private val jwtSecret: String,
    @Value("\${terralink.jwt.expiration-ms}") private val jwtExpirationMs: Long
) {
    private fun getSigningKey() = Keys.hmacShaKeyFor(jwtSecret.toByteArray(StandardCharsets.UTF_8))

    fun generateToken(userId: UUID, phoneNumber: String, role: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("phone", phoneNumber)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact()
    }

    fun getUserIdFromToken(token: String): UUID {
        val claims: Claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
        return UUID.fromString(claims.subject)
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
            true
        } catch (ex: Exception) {
            false
        }
    }
}
