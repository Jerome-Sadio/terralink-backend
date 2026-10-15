package sn.terralink.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import sn.terralink.domain.enum.ExpenseCategory
import sn.terralink.domain.enum.FarmType
import sn.terralink.domain.enum.OverallHealth
import sn.terralink.domain.enum.VisitStatus
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class CreateFarmRequest(
    @field:NotNull(message = "L'ID du propriétaire est obligatoire")
    val ownerId: UUID,

    @field:NotBlank(message = "Le nom de l'exploitation est obligatoire")
    val name: String,

    @field:NotBlank(message = "L'adresse de l'exploitation est obligatoire")
    val address: String,

    val sizeHectares: BigDecimal = BigDecimal.ZERO,

    @field:NotNull(message = "Le type d'exploitation est obligatoire")
    val type: FarmType,

    val latitude: Double? = null,
    val longitude: Double? = null
)

data class FarmResponse(
    val id: UUID,
    val ownerId: UUID,
    val ownerName: String,
    val name: String,
    val address: String,
    val sizeHectares: BigDecimal,
    val type: FarmType,
    val status: String,
    val assignedControllerId: UUID? = null,
    val assignedControllerName: String? = null
)

data class AssignControllerRequest(
    @field:NotNull(message = "L'ID de l'exploitation est obligatoire")
    val farmId: UUID,

    @field:NotNull(message = "L'ID du contrôleur est obligatoire")
    val controllerId: UUID
)

data class VisitPhotoDto(
    val photoUrl: String,
    val latitude: Double,
    val longitude: Double,
    val takenAt: OffsetDateTime,
    val metadataHash: String
)

data class VisitExpenseDto(
    val amount: BigDecimal,
    val currency: String = "XOF",
    val category: ExpenseCategory,
    val description: String? = null,
    val receiptPhotoUrl: String? = null
)

data class CreateVisitReportRequest(
    @field:NotNull(message = "L'ID de l'exploitation est obligatoire")
    val farmId: UUID,

    @field:NotNull(message = "La date de visite est obligatoire")
    val visitDate: OffsetDateTime,

    @field:NotNull(message = "L'état général est obligatoire")
    val overallHealth: OverallHealth,

    val observations: String? = null,
    val photos: List<VisitPhotoDto> = emptyList(),
    val expenses: List<VisitExpenseDto> = emptyList()
)

data class VisitReportResponse(
    val id: UUID,
    val farmId: UUID,
    val farmName: String,
    val controllerId: UUID,
    val controllerName: String,
    val status: VisitStatus,
    val visitDate: OffsetDateTime,
    val overallHealth: OverallHealth,
    val observations: String?,
    val rejectionReason: String?,
    val photos: List<VisitPhotoDto>,
    val expenses: List<VisitExpenseDto>,
    val createdAt: OffsetDateTime?
)

data class ValidateVisitRequest(
    val approved: Boolean,
    val rejectionReason: String? = null
)

data class UpdateFarmRequest(
    val name: String? = null,
    val address: String? = null,
    val sizeHectares: BigDecimal? = null,
    val type: FarmType? = null
)

data class DashboardStats(
    val totalFarms: Long,
    val totalUsers: Long,
    val pendingReports: Long,
    val totalControllers: Long,
    val totalOwners: Long,
    val totalReports: Long
)
