package sn.terralink.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sn.terralink.domain.entity.VisitReport
import sn.terralink.domain.enum.VisitStatus
import java.util.UUID

@Repository
interface VisitReportRepository : JpaRepository<VisitReport, UUID> {
    fun findAllByFarmIdOrderByVisitDateDesc(farmId: UUID): List<VisitReport>
    fun findAllByFarmIdAndStatusOrderByVisitDateDesc(farmId: UUID, status: VisitStatus): List<VisitReport>
    fun findAllByControllerIdOrderByVisitDateDesc(controllerId: UUID): List<VisitReport>
    fun findAllByStatusOrderByVisitDateDesc(status: VisitStatus): List<VisitReport>
    fun countByStatus(status: VisitStatus): Long
}
