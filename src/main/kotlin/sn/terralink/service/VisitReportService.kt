package sn.terralink.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sn.terralink.domain.entity.*
import sn.terralink.domain.enum.NotificationType
import sn.terralink.domain.enum.VisitStatus
import sn.terralink.dto.CreateVisitReportRequest
import sn.terralink.dto.ValidateVisitRequest
import sn.terralink.dto.VisitExpenseDto
import sn.terralink.dto.VisitPhotoDto
import sn.terralink.dto.VisitReportResponse
import sn.terralink.repository.*
import java.util.UUID

@Service
class VisitReportService(
    private val visitReportRepository: VisitReportRepository,
    private val farmRepository: FarmRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) {
    @Transactional
    fun submitVisitReport(controllerId: UUID, request: CreateVisitReportRequest): VisitReportResponse {
        val farm = farmRepository.findById(request.farmId)
            .orElseThrow { IllegalArgumentException("Exploitation non trouvée") }

        val controller = userRepository.findById(controllerId)
            .orElseThrow { IllegalArgumentException("Contrôleur non trouvé") }

        val visitReport = VisitReport(
            farm = farm,
            controller = controller,
            status = VisitStatus.SUBMITTED,
            visitDate = request.visitDate,
            overallHealth = request.overallHealth,
            observations = request.observations
        )

        // Attach Photos
        request.photos.forEach { photoDto ->
            visitReport.photos.add(
                VisitPhoto(
                    visitReport = visitReport,
                    photoUrl = photoDto.photoUrl,
                    latitude = photoDto.latitude,
                    longitude = photoDto.longitude,
                    takenAt = photoDto.takenAt,
                    metadataHash = photoDto.metadataHash
                )
            )
        }

        // Attach Expenses
        request.expenses.forEach { expenseDto ->
            visitReport.expenses.add(
                VisitExpense(
                    visitReport = visitReport,
                    amount = expenseDto.amount,
                    currency = expenseDto.currency,
                    category = expenseDto.category,
                    description = expenseDto.description,
                    receiptPhotoUrl = expenseDto.receiptPhotoUrl
                )
            )
        }

        val savedReport = visitReportRepository.save(visitReport)

        // Notify Admin of new submission
        val admins = userRepository.findAllByRole(sn.terralink.domain.enum.UserRole.ADMIN)
        admins.forEach { admin ->
            notificationRepository.save(
                Notification(
                    user = admin,
                    title = "Nouveau rapport de visite soumis",
                    message = "Un rapport pour l'exploitation ${farm.name} a été soumis par ${controller.firstName} ${controller.lastName}.",
                    type = NotificationType.SYSTEM
                )
            )
        }

        return mapToResponse(savedReport)
    }

    @Transactional
    fun validateVisitReport(adminId: UUID, reportId: UUID, request: ValidateVisitRequest): VisitReportResponse {
        val admin = userRepository.findById(adminId)
            .orElseThrow { IllegalArgumentException("Administrateur non trouvé") }

        val report = visitReportRepository.findById(reportId)
            .orElseThrow { IllegalArgumentException("Rapport de visite non trouvé") }

        report.validatedByAdmin = admin

        if (request.approved) {
            report.status = VisitStatus.VALIDATED

            // Notify Owner that a validated report is published
            notificationRepository.save(
                Notification(
                    user = report.farm.owner,
                    title = "Nouveau rapport disponible",
                    message = "Le rapport de visite pour votre exploitation ${report.farm.name} est désormais disponible.",
                    type = NotificationType.VISIT_PUBLISHED
                )
            )
        } else {
            report.status = VisitStatus.REJECTED
            report.rejectionReason = request.rejectionReason ?: "Rapport rejeté par l'administrateur"

            // Notify Controller of rejection
            notificationRepository.save(
                Notification(
                    user = report.controller,
                    title = "Rapport de visite rejeté",
                    message = "Votre rapport pour ${report.farm.name} a été rejeté. Motif : ${report.rejectionReason}",
                    type = NotificationType.VISIT_REJECTED
                )
            )
        }

        val updatedReport = visitReportRepository.save(report)
        return mapToResponse(updatedReport)
    }

    @Transactional(readOnly = true)
    fun getReportsForOwner(ownerId: UUID, farmId: UUID): List<VisitReportResponse> {
        val farm = farmRepository.findById(farmId)
            .orElseThrow { IllegalArgumentException("Exploitation non trouvée") }

        if (farm.owner.id != ownerId) {
            throw IllegalAccessException("Vous n'avez pas accès à cette exploitation")
        }

        // Owners only see VALIDATED reports
        return visitReportRepository.findAllByFarmIdAndStatusOrderByVisitDateDesc(farmId, VisitStatus.VALIDATED)
            .map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getReportsForAdmin(): List<VisitReportResponse> {
        return visitReportRepository.findAll().map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getReportsByController(controllerId: UUID): List<VisitReportResponse> {
        return visitReportRepository.findAllByControllerIdOrderByVisitDateDesc(controllerId)
            .map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getReportById(reportId: UUID): VisitReportResponse {
        val report = visitReportRepository.findById(reportId)
            .orElseThrow { IllegalArgumentException("Rapport non trouvé") }
        return mapToResponse(report)
    }

    private fun mapToResponse(report: VisitReport): VisitReportResponse {
        return VisitReportResponse(
            id = report.id!!,
            farmId = report.farm.id!!,
            farmName = report.farm.name,
            controllerId = report.controller.id!!,
            controllerName = "${report.controller.firstName} ${report.controller.lastName}",
            status = report.status,
            visitDate = report.visitDate,
            overallHealth = report.overallHealth,
            observations = report.observations,
            rejectionReason = report.rejectionReason,
            photos = report.photos.map {
                VisitPhotoDto(
                    photoUrl = it.photoUrl,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    takenAt = it.takenAt,
                    metadataHash = it.metadataHash
                )
            },
            expenses = report.expenses.map {
                VisitExpenseDto(
                    amount = it.amount,
                    currency = it.currency,
                    category = it.category,
                    description = it.description,
                    receiptPhotoUrl = it.receiptPhotoUrl
                )
            },
            createdAt = report.createdAt
        )
    }
}
