package sn.terralink.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import sn.terralink.domain.enum.NotificationType
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "notifications")
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(name = "title", nullable = false, length = 150)
    var title: String,

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    var message: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    var type: NotificationType,

    @Column(name = "read", nullable = false)
    var read: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: OffsetDateTime? = null
)
