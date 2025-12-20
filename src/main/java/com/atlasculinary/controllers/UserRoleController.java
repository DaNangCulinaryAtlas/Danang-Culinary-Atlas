package com.atlasculinary.controllers;

import com.atlasculinary.dtos.ApiResponse;
import com.atlasculinary.dtos.AssignRoleRequest;
import com.atlasculinary.dtos.UserRoleDto;
import com.atlasculinary.services.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "User Role Management", description = "Quản lý việc gán và duyệt Role cho User")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('USER_ROLE_VIEW')")
    public ResponseEntity<ApiResponse> getUserRoles(@PathVariable UUID userId) {
        List<UserRoleDto> roles = userRoleService.getUserRoles(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách Role thành công", roles));
    }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('USER_ROLE_ASSIGN')")
    public ResponseEntity<ApiResponse> assignRole(
            @PathVariable UUID userId,
            @RequestBody @Valid AssignRoleRequest request) {
        userRoleService.assignRoleToUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Gán Role thành công", null));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('USER_ROLE_REVOKE')")
    public ResponseEntity<ApiResponse> revokeRole(
            @PathVariable UUID userId,
            @PathVariable Long roleId) {
        userRoleService.revokeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Gỡ Role thành công", null));
    }

    @PatchMapping("/{userId}/roles/{roleId}/status")
    @PreAuthorize("hasAuthority('USER_ROLE_APPROVE')")
    @Operation(summary = "Duyệt hoặc Khóa Role", description = "Cập nhật trạng thái licensed (True/False)")
    public ResponseEntity<ApiResponse> toggleRoleStatus(
            @PathVariable UUID userId,
            @PathVariable Long roleId,
            @RequestParam boolean licensed) {

        userRoleService.toggleRoleStatus(userId, roleId, licensed);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái licensed thành công", null));
    }
}