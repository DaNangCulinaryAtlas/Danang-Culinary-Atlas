package com.atlasculinary.controllers;

import com.atlasculinary.dtos.AddBusinessLicenseRequest;
import com.atlasculinary.dtos.BusinessLicenseDto;
import com.atlasculinary.dtos.UpdateBusinessLicenseRequest;
import com.atlasculinary.dtos.UpdateLicenseStatusRequest;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.LicenseType;
import com.atlasculinary.securities.CustomAccountDetails;
import com.atlasculinary.services.BusinessLicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Tag(name = "Business License Management", description = "API for managing business licenses")
public class BusinessLicenseController {

    private final BusinessLicenseService businessLicenseService;

    // --- VENDOR ---

    @Operation(summary = "Submit a new business license (Vendor)")
    @PreAuthorize("hasAuthority('LICENSE_CREATE')")
    @PostMapping("/licenses")
    public ResponseEntity<BusinessLicenseDto> createLicense(
            @RequestBody @Valid AddBusinessLicenseRequest request,
            @AuthenticationPrincipal CustomAccountDetails principal) {
        var ownerAccountId = principal.getAccountId();
        var licenseDto = businessLicenseService.createLicense(ownerAccountId, request);
        return new ResponseEntity<>(licenseDto, HttpStatus.CREATED);
    }

    // SỬA: Đổi tên thành getMyLicenses và trả về List
    @Operation(summary = "Get my business licenses list (Vendor)")
    @PreAuthorize("hasAuthority('LICENSE_VIEW_OWN')")
    @GetMapping("/licenses/me")
    public ResponseEntity<List<BusinessLicenseDto>> getMyLicenses(
            @AuthenticationPrincipal CustomAccountDetails principal) {
        var ownerAccountId = principal.getAccountId();
        // Service giờ trả về List
        List<BusinessLicenseDto> licenseDtos = businessLicenseService.getMyLicenses(ownerAccountId);
        return ResponseEntity.ok(licenseDtos);
    }

    @Operation(summary = "Update my business license info (Vendor)")
    @PreAuthorize("hasAuthority('LICENSE_UPDATE')")
    @PatchMapping("/licenses/{licenseId}")
    public ResponseEntity<BusinessLicenseDto> updateLicense(
            @PathVariable UUID licenseId,
            @RequestBody @Valid UpdateBusinessLicenseRequest request,
            @AuthenticationPrincipal CustomAccountDetails principal) {
        var ownerAccountId = principal.getAccountId();
        var licenseDto = businessLicenseService.updateLicense(licenseId, request, ownerAccountId);
        return ResponseEntity.ok(licenseDto);
    }

    // --- ADMIN ---

    @Operation(summary = "Get all licenses with filters (Admin)")
    @PreAuthorize("hasAuthority('LICENSE_VIEW_ALL')")
    @GetMapping("/admin/licenses")
    public ResponseEntity<Page<BusinessLicenseDto>> getAllLicenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "issueDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,


            @Parameter(description = "Filter by license type (BUSINESS_REGISTRATION, FOOD_SAFETY_CERT)")
            @RequestParam(required = false) LicenseType licenseType,

            @Parameter(description = "Filter by approval status (PENDING, APPROVED, REJECTED)")
            @RequestParam(required = false) ApprovalStatus approvalStatus) {

        Page<BusinessLicenseDto> result = businessLicenseService.getAllLicenses(
                page, size, sortBy, sortDirection, licenseType, approvalStatus);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get license detail by ID (Admin)")
    @PreAuthorize("hasAuthority('LICENSE_VIEW_ALL')")
    @GetMapping("/admin/licenses/{licenseId}")
    public ResponseEntity<BusinessLicenseDto> getLicenseById(@PathVariable UUID licenseId) {
        var licenseDto = businessLicenseService.getLicenseById(licenseId);
        return ResponseEntity.ok(licenseDto);
    }

    @Operation(summary = "Approve or Reject a license (Admin)")
    @PreAuthorize("hasAuthority('LICENSE_APPROVE') or hasAuthority('LICENSE_REJECT')")
    @PatchMapping("/admin/licenses/{licenseId}/status")
    public ResponseEntity<BusinessLicenseDto> updateApprovalStatus(
            @PathVariable UUID licenseId,
            @RequestBody @Valid UpdateLicenseStatusRequest request,
            @AuthenticationPrincipal CustomAccountDetails principal) {

        var adminAccountId = principal.getAccountId();
        var updatedLicense = businessLicenseService.updateApprovalStatus(adminAccountId, licenseId, request);
        return ResponseEntity.ok(updatedLicense);
    }

    @Operation(summary = "Delete a license (Admin)")
    @PreAuthorize("hasAuthority('LICENSE_DELETE')")
    @DeleteMapping("/admin/licenses/{licenseId}")
    public ResponseEntity<Void> deleteLicense(
            @PathVariable UUID licenseId,
            @AuthenticationPrincipal CustomAccountDetails principal) {

        businessLicenseService.deleteLicense(licenseId, principal.getAccountId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}