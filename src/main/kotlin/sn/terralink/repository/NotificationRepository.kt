package sn.terralink.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sn.terralink.domain.entity.Notification
import java.util.UUID

@Repository
interface NotificationRepository : JpaRepository<Notification, UUID> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID): List<Notification>
    fun countByUserIdAndReadFalse(userId: UUID): Long
}
