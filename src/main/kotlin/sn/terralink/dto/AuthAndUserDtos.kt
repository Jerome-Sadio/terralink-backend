package sn.terralink.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import sn.terralink.domain.enum.UserRole
import java.util.UUID

data class LoginRequest(
    @field:NotBlank(message = "Le numéro de téléphone est obligatoire")
    val phoneNumber: String
)

data class VerifyPinRequest(
    @field:NotBlank(message = "Le numéro de téléphone est obligatoire")
    val phoneNumber: String,

    @field:NotBlank(message = "Le code PIN est obligatoire")
    @field:Pattern(regexp = "^[0-9]{4,6}$", message = "Le code PIN doit comporter 4 ou 6 chiffres")
    val pin: String
)

data class AuthResponse(
    val token: String,
    val userId: UUID,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole
)

data class CreateUserRequest(
    @field:NotBlank(message = "Le numéro de téléphone est obligatoire")
    val phoneNumber: String,

    @field:NotBlank(message = "Le code PIN est obligatoire")
    @field:Pattern(regexp = "^[0-9]{4,6}$", message = "Le code PIN doit comporter 4 ou 6 chiffres")
    val pin: String,

    @field:NotBlank(message = "Le prénom est obligatoire")
    val firstName: String,

    @field:NotBlank(message = "Le nom est obligatoire")
    val lastName: String,

    val role: UserRole
)

data class UserResponse(
    val id: UUID,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val status: String
)

data class UpdateUserRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val status: String? = null,
    val pin: String? = null
)

data class NotificationResponse(
    val id: UUID,
    val title: String,
    val message: String,
    val type: String,
    val read: Boolean,
    val createdAt: java.time.OffsetDateTime?
)
