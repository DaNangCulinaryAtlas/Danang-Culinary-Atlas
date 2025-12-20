package com.atlasculinary.controllers;

import com.atlasculinary.dtos.*;
import com.atlasculinary.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs quản lý định nghĩa Role (Thêm, Sửa, Xóa)")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    @Operation(summary = "Lấy danh sách Role", description = "Trả về tất cả các Role có trong hệ thống (Dùng cho dropdown hoặc trang quản lý)")
    public ResponseEntity<ApiResponse> getAllRoles() {
        List<RoleDto> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách Role thành công", roles));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @Operation(summary = "Tạo Role mới", description = "Tạo định nghĩa Role mới (VD: CONTENT_MODERATOR)")
    public ResponseEntity<ApiResponse> createRole(@RequestBody @Valid AddRoleRequest request) {
        RoleDto role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo Role thành công", role));
    }

    @PatchMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @Operation(summary = "Cập nhật Role", description = "Cập nhật mô tả cho Role (Không sửa được tên Role)")
    public ResponseEntity<ApiResponse> updateRole(
            @PathVariable Long roleId,
            @RequestBody UpdateRoleRequest request) {
        RoleDto role = roleService.updateRole(roleId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật Role thành công", role));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    @Operation(summary = "Xóa Role", description = "Xóa Role khỏi hệ thống. Chỉ xóa được khi chưa có User nào sử dụng Role này.")
    public ResponseEntity<ApiResponse> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok(ApiResponse.success("Xóa Role thành công", null));
    }
}