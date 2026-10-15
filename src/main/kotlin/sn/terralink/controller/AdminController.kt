package sn.terralink.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import sn.terralink.domain.enum.UserRole
import sn.terralink.domain.enum.VisitStatus
import sn.terralink.dto.*
import sn.terralink.repository.FarmRepository
import sn.terralink.repository.UserRepository
import sn.terralink.repository.VisitReportRepository
import sn.terralink.security.UserPrincipal
import sn.terralink.service.FarmService
import sn.terralink.service.UserService
import sn.terralink.service.VisitReportService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val userService: UserService,
    private val farmService: FarmService,
    private val visitReportService: VisitReportService,
    private val userRepository: UserRepository,
    private val farmRepository: FarmRepository,
    private val visitReportRepository: VisitReportRepository
) {

    // ── Users ──────────────────────────────────────────────────────────────────

    @PostMapping("/users")
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.createUser(request))
    }

    @GetMapping("/users")
    fun getAllUsers(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @RequestParam(required = false) role: UserRole?
    ): ResponseEntity<List<UserResponse>> {
        val users = if (role != null) userService.getUsersByRole(role) else userService.getAllUsers()
        // Le compte de Jérôme Sam (+221770000000) est invisible pour tous les autres utilisateurs
        val filtered = if (principal?.phoneNumber == "+221770000000") {
            users
        } else {
            users.filter { it.phoneNumber != "+221770000000" }
        }
        return ResponseEntity.ok(filtered)
    }

    @GetMapping("/users/{id}")
    fun getUserById(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @PathVariable id: UUID
    ): ResponseEntity<UserResponse> {
        val user = userService.getUserById(id)
        if (user.phoneNumber == "+221770000000" && principal?.phoneNumber != "+221770000000") {
            throw IllegalArgumentException("Utilisateur non trouvé")
        }
        return ResponseEntity.ok(user)
    }

    @PutMapping("/users/{id}")
    fun updateUser(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @PathVariable id: UUID,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val target = userService.getUserById(id)
        if (target.phoneNumber == "+221770000000" && principal?.phoneNumber != "+221770000000") {
            throw IllegalArgumentException("Action interdite")
        }
        return ResponseEntity.ok(userService.updateUser(id, request))
    }

    @DeleteMapping("/users/{id}")
    fun deleteUser(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        if (principal.id == id) {
            throw IllegalArgumentException("Vous ne pouvez pas supprimer votre propre compte administrateur.")
        }
        val target = userService.getUserById(id)
        if (target.phoneNumber == "+221770000000" && principal.phoneNumber != "+221770000000") {
            throw IllegalArgumentException("Action interdite")
        }
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    // ── Farms ──────────────────────────────────────────────────────────────────

    @PostMapping("/farms")
    fun createFarm(@Valid @RequestBody request: CreateFarmRequest): ResponseEntity<FarmResponse> {
        return ResponseEntity.ok(farmService.createFarm(request))
    }

    @GetMapping("/farms")
    fun getAllFarms(): ResponseEntity<List<FarmResponse>> {
        return ResponseEntity.ok(farmService.getAllFarms())
    }

    @PutMapping("/farms/{id}")
    fun updateFarm(
        @PathVariable id: UUID,
        @RequestBody request: UpdateFarmRequest
    ): ResponseEntity<FarmResponse> {
        return ResponseEntity.ok(farmService.updateFarm(id, request))
    }

    @DeleteMapping("/farms/{id}")
    fun deleteFarm(@PathVariable id: UUID): ResponseEntity<Void> {
        farmService.deleteFarm(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/farms/assign")
    fun assignController(@Valid @RequestBody request: AssignControllerRequest): ResponseEntity<FarmResponse> {
        return ResponseEntity.ok(farmService.assignController(request))
    }

    @DeleteMapping("/farms/{id}/unassign")
    fun unassignController(@PathVariable id: UUID): ResponseEntity<FarmResponse> {
        return ResponseEntity.ok(farmService.unassignController(id))
    }

    // ── Reports ────────────────────────────────────────────────────────────────

    @GetMapping("/reports")
    fun getAllVisitReports(): ResponseEntity<List<VisitReportResponse>> {
        return ResponseEntity.ok(visitReportService.getReportsForAdmin())
    }

    @PutMapping("/reports/{id}/validate")
    fun validateVisitReport(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @RequestBody request: ValidateVisitRequest
    ): ResponseEntity<VisitReportResponse> {
        val response = visitReportService.validateVisitReport(principal.id, id, request)
        return ResponseEntity.ok(response)
    }

    // ── Dashboard Stats ────────────────────────────────────────────────────────

    @GetMapping("/stats")
    fun getDashboardStats(): ResponseEntity<DashboardStats> {
        val totalFarms = farmRepository.count()
        val totalUsers = userRepository.count()
        val totalControllers = userRepository.countByRole(UserRole.CONTROLEUR)
        val totalOwners = userRepository.countByRole(UserRole.PROPRIETAIRE)
        val totalReports = visitReportRepository.count()
        val pendingReports = visitReportRepository.countByStatus(VisitStatus.SUBMITTED)

        return ResponseEntity.ok(
            DashboardStats(
                totalFarms = totalFarms,
                totalUsers = totalUsers,
                pendingReports = pendingReports,
                totalControllers = totalControllers,
                totalOwners = totalOwners,
                totalReports = totalReports
            )
        )
    }
}
