package sn.terralink.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import sn.terralink.domain.enum.FarmStatus
import sn.terralink.domain.enum.FarmType
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "farms")
class Farm(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User,

    @Column(name = "name", nullable = false, length = 150)
    var name: String,

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    var address: String,

    @Column(name = "size_hectares", nullable = false, precision = 10, scale = 2)
    var sizeHectares: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    var type: FarmType,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: FarmStatus = FarmStatus.ACTIVE,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: OffsetDateTime? = null
)
