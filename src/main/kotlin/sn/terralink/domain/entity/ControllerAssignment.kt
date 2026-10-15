package sn.terralink.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "controller_assignments")
class ControllerAssignment(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    var farm: Farm,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controller_id", nullable = false)
    var controller: User,

    @CreationTimestamp
    @Column(name = "assigned_at", updatable = false)
    var assignedAt: OffsetDateTime? = null,

    @Column(name = "unassigned_at")
    var unassignedAt: OffsetDateTime? = null,

    @Column(name = "active", nullable = false)
    var active: Boolean = true
)
