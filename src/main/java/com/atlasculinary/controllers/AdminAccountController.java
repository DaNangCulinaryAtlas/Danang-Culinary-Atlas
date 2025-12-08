package com.atlasculinary.controllers;

import com.atlasculinary.dtos.*;
import com.atlasculinary.enums.AccountStatus;
import com.atlasculinary.services.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
@Tag(name = "Admin Account Management", description = "API for managing user and vendor accounts")
public class AdminAccountController {

    private final UserManagementService userManagementService;

    @Operation(summary = "Get list of users")
    @PreAuthorize("hasAuthority('ADMIN_VIEW')")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getUsers(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountListDto> users = userManagementService.getUsers(status, search, pageable);
        
        ApiResponse response = ApiResponse.success(
            "Users retrieved successfully",
            users
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get list of vendors")
    @PreAuthorize("hasAuthority('ADMIN_VIEW')")
    @GetMapping("/vendors")
    public ResponseEntity<ApiResponse> getVendors(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountListDto> vendors = userManagementService.getVendors(status, search, pageable);
        
        ApiResponse response = ApiResponse.success(
            "Vendors retrieved successfully",
            vendors
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get account detail by ID")
    @PreAuthorize("hasAuthority('ADMIN_VIEW')")
    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse> getAccountDetail(@PathVariable UUID accountId) {
        AccountDetailDto accountDetail = userManagementService.getAccountDetail(accountId);
        
        ApiResponse response = ApiResponse.success(
            "Account detail retrieved successfully",
            accountDetail
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update account status")
    @PreAuthorize("hasAuthority('ADMIN_EDIT')")
    @PatchMapping("/{accountId}/status")
    public ResponseEntity<ApiResponse> updateAccountStatus(
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateAccountStatusRequest request) {
        
        userManagementService.updateAccountStatus(accountId, request);
        
        ApiResponse response = ApiResponse.success("Account status updated successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Send email to account")
    @PreAuthorize("hasAuthority('ADMIN_EDIT')")
    @PostMapping("/{accountId}/send-email")
    public ResponseEntity<ApiResponse> sendEmailToAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody SendEmailRequest request) {
        
        userManagementService.sendEmailToAccount(accountId, request.getSubject(), request.getContent());
        
        ApiResponse response = ApiResponse.success("Email sent successfully");
        return ResponseEntity.ok(response);
    }
}
