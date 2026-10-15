package sn.terralink.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sn.terralink.domain.entity.User
import sn.terralink.domain.enum.UserRole
import java.util.Optional
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByPhoneNumber(phoneNumber: String): Optional<User>
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    fun findAllByRole(role: UserRole): List<User>
    fun countByRole(role: UserRole): Long
}
