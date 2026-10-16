package sn.terralink.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sn.terralink.domain.entity.ControllerAssignment
import sn.terralink.domain.entity.Farm
import sn.terralink.dto.AssignControllerRequest
import sn.terralink.dto.CreateFarmRequest
import sn.terralink.dto.FarmResponse
import sn.terralink.dto.UpdateFarmRequest
import sn.terralink.repository.ControllerAssignmentRepository
import sn.terralink.repository.FarmRepository
import sn.terralink.repository.UserRepository
import java.util.UUID

@Service
class FarmService(
    private val farmRepository: FarmRepository,
    private val userRepository: UserRepository,
    private val assignmentRepository: ControllerAssignmentRepository
) {
    @Transactional
    fun createFarm(request: CreateFarmRequest): FarmResponse {
        val owner = userRepository.findById(request.ownerId)
            .orElseThrow { IllegalArgumentException("Propriétaire non trouvé") }

        val farm = Farm(
            owner = owner,
            name = request.name,
            address = request.address,
            sizeHectares = request.sizeHectares,
            type = request.type,
            latitude = request.latitude,
            longitude = request.longitude
        )

        val savedFarm = farmRepository.save(farm)
        return mapToResponse(savedFarm)
    }

    @Transactional
    fun assignController(request: AssignControllerRequest): FarmResponse {
        val farm = farmRepository.findById(request.farmId)
            .orElseThrow { IllegalArgumentException("Exploitation non trouvée") }

        val controller = userRepository.findById(request.controllerId)
            .orElseThrow { IllegalArgumentException("Contrôleur non trouvé") }

        // Deactivate existing assignment if any
        assignmentRepository.findByFarmIdAndActiveTrue(request.farmId).ifPresent { existing ->
            existing.active = false
            assignmentRepository.save(existing)
        }

        val newAssignment = ControllerAssignment(
            farm = farm,
            controller = controller,
            active = true
        )
        assignmentRepository.save(newAssignment)

        return mapToResponse(farm)
    }

    @Transactional(readOnly = true)
    fun getAllFarms(): List<FarmResponse> {
        return farmRepository.findAll().map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getFarmsByOwner(ownerId: UUID): List<FarmResponse> {
        return farmRepository.findAllByOwnerId(ownerId).map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getFarmsByController(controllerId: UUID): List<FarmResponse> {
        val assignments = assignmentRepository.findAllByControllerIdAndActiveTrue(controllerId)
        return assignments.map { mapToResponse(it.farm) }
    }

    @Transactional
    fun deleteFarm(farmId: UUID) {
        if (!farmRepository.existsById(farmId)) {
            throw IllegalArgumentException("Exploitation non trouvée")
        }
        farmRepository.deleteById(farmId)
    }

    @Transactional
    fun updateFarm(farmId: UUID, request: UpdateFarmRequest): FarmResponse {
        val farm = farmRepository.findById(farmId)
            .orElseThrow { IllegalArgumentException("Exploitation non trouvée") }

        request.name?.let { farm.name = it }
        request.address?.let { farm.address = it }
        request.sizeHectares?.let { farm.sizeHectares = it }
        request.type?.let { farm.type = it }
        request.latitude?.let { farm.latitude = it }
        request.longitude?.let { farm.longitude = it }

        return mapToResponse(farmRepository.save(farm))
    }

    @Transactional
    fun unassignController(farmId: UUID): FarmResponse {
        val farm = farmRepository.findById(farmId)
            .orElseThrow { IllegalArgumentException("Exploitation non trouvée") }

        assignmentRepository.findByFarmIdAndActiveTrue(farmId).ifPresent { existing ->
            existing.active = false
            assignmentRepository.save(existing)
        }

        return mapToResponse(farm)
    }

    private fun mapToResponse(farm: Farm): FarmResponse {
        val activeAssignment = assignmentRepository.findByFarmIdAndActiveTrue(farm.id!!).orElse(null)

        return FarmResponse(
            id = farm.id!!,
            ownerId = farm.owner.id!!,
            ownerName = "${farm.owner.firstName} ${farm.owner.lastName}",
            name = farm.name,
            address = farm.address,
            sizeHectares = farm.sizeHectares,
            type = farm.type,
            status = farm.status.name,
            assignedControllerId = activeAssignment?.controller?.id,
            assignedControllerName = activeAssignment?.let { "${it.controller.firstName} ${it.controller.lastName}" },
            latitude = farm.latitude,
            longitude = farm.longitude
        )
    }
}
