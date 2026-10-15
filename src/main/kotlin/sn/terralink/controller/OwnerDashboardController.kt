package sn.terralink.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import sn.terralink.dto.FarmResponse
import sn.terralink.dto.VisitReportResponse
import sn.terralink.security.UserPrincipal
import sn.terralink.service.FarmService
import sn.terralink.service.VisitReportService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/owner")
class OwnerDashboardController(
    private val farmService: FarmService,
    private val visitReportService: VisitReportService
) {

    @GetMapping("/farms")
    fun getMyFarms(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<FarmResponse>> {
        val farms = farmService.getFarmsByOwner(principal.id)
        return ResponseEntity.ok(farms)
    }

    @GetMapping("/farms/{farmId}/reports")
    fun getFarmReports(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable farmId: UUID
    ): ResponseEntity<List<VisitReportResponse>> {
        val reports = visitReportService.getReportsForOwner(principal.id, farmId)
        return ResponseEntity.ok(reports)
    }
}
