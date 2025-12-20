package com.atlasculinary.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRolePermissionRequest {
    @NotNull(message = "Role ID là bắt buộc")
    private Long roleId;

    @NotEmpty(message = "Danh sách quyền không được để trống")
    private List<Long> actionIds;
}