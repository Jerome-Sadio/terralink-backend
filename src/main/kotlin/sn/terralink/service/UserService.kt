package sn.terralink.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sn.terralink.domain.entity.User
import sn.terralink.domain.enum.UserRole
import sn.terralink.dto.CreateUserRequest
import sn.terralink.dto.NotificationResponse
import sn.terralink.dto.UpdateUserRequest
import sn.terralink.dto.UserResponse
import sn.terralink.repository.NotificationRepository
import sn.terralink.repository.UserRepository
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        if (userRepository.existsByPhoneNumber(request.phoneNumber)) {
            throw IllegalArgumentException("Un utilisateur existe déjà avec ce numéro de téléphone")
        }

        val newUser = User(
            phoneNumber = request.phoneNumber,
            pinHash = passwordEncoder.encode(request.pin),
            role = request.role,
            firstName = request.firstName,
            lastName = request.lastName
        )

        val savedUser = userRepository.save(newUser)
        return mapToResponse(savedUser)
    }

    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getUsersByRole(role: UserRole): List<UserResponse> {
        return userRepository.findAllByRole(role).map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getUserById(id: UUID): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Utilisateur non trouvé") }
        return mapToResponse(user)
    }

    @Transactional
    fun deleteUser(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw IllegalArgumentException("Utilisateur non trouvé")
        }
        userRepository.deleteById(id)
    }

    @Transactional
    fun updateUser(id: UUID, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Utilisateur non trouvé") }

        request.firstName?.let { user.firstName = it }
        request.lastName?.let { user.lastName = it }
        request.status?.let {
            user.status = sn.terralink.domain.enum.UserStatus.valueOf(it)
        }
        request.pin?.let { user.pinHash = passwordEncoder.encode(it) }

        return mapToResponse(userRepository.save(user))
    }

    @Transactional(readOnly = true)
    fun getNotificationsForUser(userId: UUID): List<NotificationResponse> {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map {
            NotificationResponse(
                id = it.id!!,
                title = it.title,
                message = it.message,
                type = it.type.name,
                read = it.read,
                createdAt = it.createdAt
            )
        }
    }

    @Transactional
    fun markNotificationRead(userId: UUID, notificationId: UUID) {
        val notif = notificationRepository.findById(notificationId)
            .orElseThrow { IllegalArgumentException("Notification non trouvée") }
        if (notif.user.id == userId) {
            notif.read = true
            notificationRepository.save(notif)
        }
    }

    @Transactional
    fun markAllNotificationsRead(userId: UUID) {
        val notifs = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        notifs.forEach { it.read = true }
        notificationRepository.saveAll(notifs)
    }

    private fun mapToResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id!!,
            phoneNumber = user.phoneNumber,
            firstName = user.firstName,
            lastName = user.lastName,
            role = user.role,
            status = user.status.name
        )
    }
}
