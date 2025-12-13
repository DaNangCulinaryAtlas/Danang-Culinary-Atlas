package com.atlasculinary.controllers;

import com.atlasculinary.dtos.AdminDto;
import com.atlasculinary.dtos.ApiResponse;
import com.atlasculinary.dtos.UserDto;
import com.atlasculinary.dtos.VendorDto;
import com.atlasculinary.dtos.VendorOverviewDto;
import com.atlasculinary.dtos.profile.*;
import com.atlasculinary.services.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile Management", description = "API for viewing and updating user, admin, and vendor profiles")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Get current user profile", description = "Retrieve profile information for the currently authenticated user")
    @GetMapping("/user")
    @PreAuthorize("hasAuthority('PROFILE_USER_VIEW')")
    public ResponseEntity<ApiResponse> getUserProfile(Authentication authentication) {
        String email = authentication.getName();
        UserDto profile = profileService.getUserProfile(email);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", profile));
    }

    @Operation(summary = "Update current user profile", description = "Update profile information for the currently authenticated user")
    @PatchMapping("/user")
    @PreAuthorize("hasAuthority('PROFILE_USER_UPDATE')")
    public ResponseEntity<ApiResponse> updateUserProfile(
            @Valid @RequestBody UserProfileUpdateDto updateDto,
            Authentication authentication) {
        String email = authentication.getName();
        UserDto profile = profileService.updateUserProfile(email, updateDto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin người dùng thành công", profile));
    }

    @Operation(summary = "Get current admin profile", description = "Retrieve profile information for the currently authenticated admin")
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('PROFILE_ADMIN_VIEW')")
    public ResponseEntity<ApiResponse> getAdminProfile(Authentication authentication) {
        String email = authentication.getName();
        AdminDto profile = profileService.getAdminProfile(email);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin quản trị viên thành công", profile));
    }

    @Operation(summary = "Update current admin profile", description = "Update profile information for the currently authenticated admin")
    @PatchMapping("/admin")
    @PreAuthorize("hasAuthority('PROFILE_ADMIN_UPDATE')")
    public ResponseEntity<ApiResponse> updateAdminProfile(
            @Valid @RequestBody AdminProfileUpdateDto updateDto,
            Authentication authentication) {
        String email = authentication.getName();
        AdminDto profile = profileService.updateAdminProfile(email, updateDto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin admin thành công", profile));
    }

    @Operation(summary = "Get current vendor profile", description = "Retrieve profile information for the currently authenticated vendor")
    @GetMapping("/vendor")
    @PreAuthorize("hasAuthority('PROFILE_VENDOR_VIEW')")
    public ResponseEntity<ApiResponse> getVendorProfile(Authentication authentication) {
        String email = authentication.getName();
        VendorDto profile = profileService.getVendorProfile(email);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhà cung cấp thành công", profile));
    }

    @Operation(summary = "Update current vendor profile", description = "Update profile information for the currently authenticated vendor")
    @PatchMapping("/vendor")
    @PreAuthorize("hasAuthority('PROFILE_VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse> updateVendorProfile(
            @Valid @RequestBody VendorProfileUpdateDto updateDto,
            Authentication authentication) {
        String email = authentication.getName();
        VendorDto profile = profileService.updateVendorProfile(email, updateDto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin vendor thành công", profile));
    }

    @Operation(summary = "Get vendor overview statistics", description = "Retrieve overview statistics for the currently authenticated vendor including total restaurants, dishes, and reviews")
    @GetMapping("/vendor/overview")
    @PreAuthorize("hasAuthority('PROFILE_VENDOR_VIEW')")
    public ResponseEntity<ApiResponse> getVendorOverview(Authentication authentication) {
        String email = authentication.getName();
        VendorOverviewDto overview = profileService.getVendorOverview(email);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tổng quan thành công", overview));
    }
}