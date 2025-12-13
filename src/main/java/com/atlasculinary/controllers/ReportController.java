package com.atlasculinary.controllers;

import com.atlasculinary.dtos.ReportRequest;
import com.atlasculinary.dtos.ReportResponse;
import com.atlasculinary.dtos.ReportStatisticsResponse;
import com.atlasculinary.dtos.UpdateReportStatusRequest;
import com.atlasculinary.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Report Management", description = "API for creating, viewing, and moderating reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @Operation(summary = "Create a new report", description = "Create a new report for a restaurant, dish, or review by the current user")
    @PostMapping("/reports")
    @PreAuthorize("hasAuthority('REPORT_CREATE')")
    public ResponseEntity<ReportResponse> createReport(@RequestBody ReportRequest request, Authentication authentication) {
        String username = authentication.getName();
        ReportResponse response = reportService.createReport(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get current user's reports", description = "Retrieve all reports created by the currently authenticated user")
    @GetMapping("/reports/my")
    @PreAuthorize("hasAuthority('REPORT_VIEW_OWN')")
    public ResponseEntity<List<ReportResponse>> getMyReports(Authentication authentication) {
        String username = authentication.getName();
        List<ReportResponse> reports = reportService.getReportsByReporter(username);
        return ResponseEntity.ok(reports);
    }

    @Operation(summary = "Admin: Get all reports", description = "Retrieve all reports in the system for administrative review")
    @GetMapping("/admin/reports")
    @PreAuthorize("hasAuthority('REPORT_VIEW_ALL')")
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        List<ReportResponse> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    @Operation(summary = "Admin: Update report status", description = "Update the status of a specific report (e.g., PENDING, RESOLVED, REJECTED)")
    @PutMapping("/admin/reports/{reportId}/status")
    @PreAuthorize("hasAuthority('REPORT_UPDATE_STATUS')")
    public ResponseEntity<ReportResponse> updateReportStatus(@PathVariable UUID reportId, @RequestBody UpdateReportStatusRequest request, Authentication authentication) {
        String adminUsername = authentication.getName();
        ReportResponse response = reportService.updateReportStatus(reportId, request.getStatus(), adminUsername);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Admin: Get report statistics", description = "Retrieve aggregated statistics for all reports")
    @GetMapping("/admin/reports/statistics")
    @PreAuthorize("hasAuthority('REPORT_VIEW_STATISTICS')")
    public ResponseEntity<ReportStatisticsResponse> getReportStatistics() {
        ReportStatisticsResponse stats = reportService.getReportStatistics();
        return ResponseEntity.ok(stats);
    }
}
