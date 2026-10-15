package sn.terralink.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sn.terralink.domain.entity.ControllerAssignment
import java.util.Optional
import java.util.UUID

@Repository
interface ControllerAssignmentRepository : JpaRepository<ControllerAssignment, UUID> {
    fun findAllByControllerIdAndActiveTrue(controllerId: UUID): List<ControllerAssignment>
    fun findByFarmIdAndActiveTrue(farmId: UUID): Optional<ControllerAssignment>
}
