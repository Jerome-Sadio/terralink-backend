package sn.terralink.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import sn.terralink.domain.enum.OverallHealth
import sn.terralink.domain.enum.VisitStatus
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "visit_reports")
class VisitReport(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    var farm: Farm,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controller_id", nullable = false)
    var controller: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by_admin_id")
    var validatedByAdmin: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    var status: VisitStatus = VisitStatus.SUBMITTED,

    @Column(name = "visit_date", nullable = false)
    var visitDate: OffsetDateTime,

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_health", nullable = false, length = 20)
    var overallHealth: OverallHealth,

    @Column(name = "observations", columnDefinition = "TEXT")
    var observations: String? = null,

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    var rejectionReason: String? = null,

    @OneToMany(mappedBy = "visitReport", cascade = [CascadeType.ALL], orphanRemoval = true)
    var photos: MutableList<VisitPhoto> = mutableListOf(),

    @OneToMany(mappedBy = "visitReport", cascade = [CascadeType.ALL], orphanRemoval = true)
    var expenses: MutableList<VisitExpense> = mutableListOf(),

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime? = null
)
