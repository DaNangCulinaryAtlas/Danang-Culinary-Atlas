package com.atlasculinary.controllers;

import com.atlasculinary.dtos.*;
import com.atlasculinary.services.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "APIs for managing role-based permissions")
public class PermissionController {
    private final PermissionService permissionService;

    @GetMapping("/actions")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    @Operation(summary = "Get all available actions", description = "Retrieve all actions in the system that can be assigned to roles")
    public ResponseEntity<ApiResponse> getAllActions() {
        List<ActionDto> actions = permissionService.getAllActions();
        return ResponseEntity.ok(ApiResponse.success("Actions retrieved successfully", actions));
    }

    @GetMapping("/roles-with-permissions")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    @Operation(summary = "Get all roles with their permissions", description = "Retrieve all roles along with their assigned actions/permissions")
    public ResponseEntity<ApiResponse> getAllRolesWithPermissions() {
        List<RolePermissionDto> rolesWithPermissions = permissionService.getAllRolesWithPermissions();
        return ResponseEntity.ok(ApiResponse.success("Roles with permissions retrieved successfully", rolesWithPermissions));
    }

    @GetMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    @Operation(summary = "Get permissions for a specific role", description = "Retrieve all permissions assigned to a specific role")
    public ResponseEntity<ApiResponse> getRolePermissions(@PathVariable Long roleId) {
        RolePermissionDto rolePermissions = permissionService.getRolePermissions(roleId);
        return ResponseEntity.ok(ApiResponse.success("Role permissions retrieved successfully", rolePermissions));
    }

    @PatchMapping("/roles/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    @Operation(summary = "Update role permissions", description = "Update the permissions (actions) assigned to a specific role. This will replace all existing permissions.")
    public ResponseEntity<ApiResponse> updateRolePermissions(
            @RequestBody UpdateRolePermissionRequest request) {
        RolePermissionDto updatedPermissions = permissionService.updateRolePermissions(request);
        return ResponseEntity.ok(ApiResponse.success("Role permissions updated successfully", updatedPermissions));
    }

    @Operation(summary = "Cấu hình mặc đinh- nâng cao Action cho một Role cụ thể")
    @PatchMapping("/roles/{roleId}/actions/{actionId}/configuration")
    @PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse> updateRoleActionConfig(
            @PathVariable Long roleId,
            @PathVariable Long actionId,
            @RequestParam boolean requiresLicense) {

        permissionService.updateRoleActionConfig(roleId, actionId, requiresLicense);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật cấu hình thành công", null));
    }
}