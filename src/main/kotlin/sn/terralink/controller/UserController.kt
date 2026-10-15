package sn.terralink.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import sn.terralink.domain.enum.UserRole
import sn.terralink.dto.NotificationResponse
import sn.terralink.dto.UpdateUserRequest
import sn.terralink.dto.UserResponse
import sn.terralink.security.UserPrincipal
import sn.terralink.service.UserService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.getUserById(principal.id))
    }

    @PutMapping("/me")
    fun updateMyProfile(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        // Prevent regular users from altering their status directly
        val safeRequest = UpdateUserRequest(
            firstName = request.firstName,
            lastName = request.lastName,
            pin = request.pin,
            status = null
        )
        return ResponseEntity.ok(userService.updateUser(principal.id, safeRequest))
    }

    @DeleteMapping("/me")
    fun deleteMyAccount(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<Void> {
        if (principal.role == UserRole.ADMIN.name) {
            throw IllegalArgumentException("Le compte administrateur racine ne peut pas être supprimé.")
        }
        userService.deleteUser(principal.id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/notifications")
    fun getMyNotifications(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<NotificationResponse>> {
        return ResponseEntity.ok(userService.getNotificationsForUser(principal.id))
    }

    @PutMapping("/notifications/{id}/read")
    fun markNotificationAsRead(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        userService.markNotificationRead(principal.id, id)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/notifications/read-all")
    fun markAllNotificationsAsRead(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Void> {
        userService.markAllNotificationsRead(principal.id)
        return ResponseEntity.ok().build()
    }
}
