package sn.terralink.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.http.MediaType
import org.springframework.web.multipart.MultipartFile
import sn.terralink.dto.CreateVisitReportRequest
import sn.terralink.dto.FarmResponse
import sn.terralink.dto.VisitReportResponse
import sn.terralink.security.UserPrincipal
import sn.terralink.service.FarmService
import sn.terralink.service.MinioService
import sn.terralink.service.VisitReportService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/controller")
class ControllerVisitController(
    private val farmService: FarmService,
    private val visitReportService: VisitReportService,
    private val minioService: MinioService
) {

    @PostMapping("/photos/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadInspectionPhoto(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam("photo") photo: MultipartFile,
        @RequestParam(value = "latitude", defaultValue = "0.0") latitude: Double,
        @RequestParam(value = "longitude", defaultValue = "0.0") longitude: Double
    ): ResponseEntity<Map<String, Any>> {
        val result = minioService.uploadPhoto(photo, "inspections/${principal.id}")
        return ResponseEntity.ok(
            mapOf(
                "photoUrl" to result.photoUrl,
                "metadataHash" to result.metadataHash,
                "latitude" to latitude,
                "longitude" to longitude,
                "sizeBytes" to result.sizeBytes
            )
        )
    }

    @GetMapping("/assigned-farms")
    fun getMyAssignedFarms(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<FarmResponse>> {
        val farms = farmService.getFarmsByController(principal.id)
        return ResponseEntity.ok(farms)
    }

    @PostMapping("/reports")
    fun submitVisitReport(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: CreateVisitReportRequest
    ): ResponseEntity<VisitReportResponse> {
        val response = visitReportService.submitVisitReport(principal.id, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/reports")
    fun getMyReports(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<VisitReportResponse>> {
        val reports = visitReportService.getReportsByController(principal.id)
        return ResponseEntity.ok(reports)
    }

    @GetMapping("/reports/{id}")
    fun getReportById(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<VisitReportResponse> {
        val report = visitReportService.getReportById(id)
        return ResponseEntity.ok(report)
    }
}
