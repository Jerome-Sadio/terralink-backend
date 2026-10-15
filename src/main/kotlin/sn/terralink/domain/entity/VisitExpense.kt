package sn.terralink.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import sn.terralink.domain.enum.ExpenseCategory
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "visit_expenses")
class VisitExpense(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_report_id", nullable = false)
    var visitReport: VisitReport,

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    var amount: BigDecimal,

    @Column(name = "currency", nullable = false, length = 10)
    var currency: String = "XOF",

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    var category: ExpenseCategory,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "receipt_photo_url", columnDefinition = "TEXT")
    var receiptPhotoUrl: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: OffsetDateTime? = null
)
