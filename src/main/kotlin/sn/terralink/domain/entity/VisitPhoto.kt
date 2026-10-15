package sn.terralink.domain.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "visit_photos")
class VisitPhoto(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_report_id", nullable = false)
    var visitReport: VisitReport,

    @Column(name = "photo_url", nullable = false, columnDefinition = "TEXT")
    var photoUrl: String,

    @Column(name = "latitude", nullable = false)
    var latitude: Double,

    @Column(name = "longitude", nullable = false)
    var longitude: Double,

    @Column(name = "taken_at", nullable = false)
    var takenAt: OffsetDateTime,

    @Column(name = "metadata_hash", nullable = false, length = 64)
    var metadataHash: String
)
