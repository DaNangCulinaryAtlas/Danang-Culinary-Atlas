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

    @Operation(summary = "Submit a new business license (Vendor) BUSINESS_REGISTRATION FOOD_SAFETY_CERT")
    @PreAuthorize("hasAuthority('LICENSE_CREATE')")
    @PostMapping("/licenses")
    public ResponseEntity<BusinessLicenseDto> createLicense(
            @RequestBody @Valid AddBusinessLicenseRequest request,
            @AuthenticationPrincipal CustomAccountDetails principal) {

        var licenseDto = businessLicenseService.createLicense(principal.getAccountId(), request);
        return new ResponseEntity<>(licenseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all licenses owned by current user (Vendor)")
    @PreAuthorize("hasAuthority('LICENSE_VIEW_OWN')")
    @GetMapping("/licenses/me")
    public ResponseEntity<List<BusinessLicenseDto>> getMyLicenses(
            @AuthenticationPrincipal CustomAccountDetails principal) {
        List<BusinessLicenseDto> licenseDtos = businessLicenseService.getMyLicenses(principal.getAccountId());
        return ResponseEntity.ok(licenseDtos);
    }

    @Operation(summary = "Get licenses by Restaurant ID (Vendor/Admin)")
    @PreAuthorize("hasAuthority('LICENSE_VIEW_OWN') or hasAuthority('LICENSE_VIEW_ALL')")
    @GetMapping("/restaurants/{restaurantId}/licenses")
    public ResponseEntity<List<BusinessLicenseDto>> getLicensesByRestaurant(
            @PathVariable UUID restaurantId,
            @AuthenticationPrincipal CustomAccountDetails principal) {

        List<BusinessLicenseDto> licenseDtos = businessLicenseService.getLicensesByRestaurant(restaurantId, principal.getAccountId());

        return ResponseEntity.ok(licenseDtos);
    }

    @Operation(summary = "Update my business license info (Vendor)")
    @PreAuthorize("hasAuthority('LICENSE_UPDATE')")
    @PatchMapping("/licenses/{licenseId}")
    public ResponseEntity<BusinessLicenseDto> updateLicense(
            @PathVariable UUID licenseId,
            @RequestBody @Valid UpdateBusinessLicenseRequest request,
            @AuthenticationPrincipal CustomAccountDetails principal) {
        var licenseDto = businessLicenseService.updateLicense(licenseId, request, principal.getAccountId());
        return ResponseEntity.ok(licenseDto);
    }

    @Operation(summary = "Delete a license (Admin/Owner)")
    @PreAuthorize("hasAuthority('LICENSE_DELETE')")
    @DeleteMapping("/licenses/{licenseId}")
    public ResponseEntity<Void> deleteLicense(
            @PathVariable UUID licenseId,
            @AuthenticationPrincipal CustomAccountDetails principal) {

        businessLicenseService.deleteLicense(licenseId, principal.getAccountId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get all licenses with filters (Admin)")
    @PreAuthorize("hasAuthority('LICENSE_VIEW_ALL')")
    @GetMapping("/admin/licenses")
    public ResponseEntity<Page<BusinessLicenseDto>> getAllLicenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "issueDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,

            @Parameter(description = "Filter by license type")
            @RequestParam(required = false) LicenseType licenseType,

            @Parameter(description = "Filter by approval status")
            @RequestParam(required = false) ApprovalStatus approvalStatus) {

        Page<BusinessLicenseDto> result = businessLicenseService.getAllLicenses(
                page, size, sortBy, sortDirection, licenseType, approvalStatus);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get license detail by ID (Admin & Owner)")
    @PreAuthorize("hasAuthority('LICENSE_VIEW_ALL') or hasAuthority('LICENSE_VIEW_OWN')")
    @GetMapping("/licenses/{licenseId}")
    public ResponseEntity<BusinessLicenseDto> getLicenseById(
            @PathVariable UUID licenseId,
            @AuthenticationPrincipal CustomAccountDetails principal) {

        var licenseDto = businessLicenseService.getLicenseById(licenseId, principal.getAccountId());
        return ResponseEntity.ok(licenseDto);
    }

    @Operation(summary = "Approve or Reject a license (Admin)")
    @PreAuthorize("hasAuthority('LICENSE_APPROVE') or hasAuthority('LICENSE_REJECT')")
    @PatchMapping("/admin/licenses/{licenseId}/status")
    public ResponseEntity<BusinessLicenseDto> updateApprovalStatus(
            @PathVariable UUID licenseId,
            @RequestBody @Valid UpdateLicenseStatusRequest request,
            @AuthenticationPrincipal CustomAccountDetails principal) {

        var updatedLicense = businessLicenseService.updateApprovalStatus(principal.getAccountId(), licenseId, request);
        return ResponseEntity.ok(updatedLicense);
    }
}