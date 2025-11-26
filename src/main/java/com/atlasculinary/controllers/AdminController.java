package com.atlasculinary.controllers;

import com.atlasculinary.dtos.AdminOverviewDto;
import com.atlasculinary.dtos.ApiResponse;
import com.atlasculinary.dtos.RestaurantCountByTagDto;
import com.atlasculinary.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@AllArgsConstructor
@Tag(name = "Admin Management", description = "API for admin operations and statistics")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Get admin overview statistics")
    @PreAuthorize("hasAuthority('ADMIN_VIEW')")
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse> getAdminOverview() {
        AdminOverviewDto overview = adminService.getAdminOverview();
        ApiResponse response = ApiResponse.success(
            "Admin overview retrieved successfully",
            overview
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get restaurant count by tag")
    @PreAuthorize("hasAuthority('ADMIN_VIEW')")
    @GetMapping("/restaurants/count-by-tag")
    public ResponseEntity<ApiResponse> getRestaurantCountByTag() {
        List<RestaurantCountByTagDto> counts = adminService.getRestaurantCountByTag();
        ApiResponse response = ApiResponse.success(
            "Restaurant count by tag retrieved successfully",
            counts
        );
        return ResponseEntity.ok(response);
    }
}
