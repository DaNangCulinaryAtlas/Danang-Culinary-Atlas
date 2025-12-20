package com.atlasculinary.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddRoleRequest {
    @NotBlank(message = "Tên Role không được để trống")
    private String roleName;

    private String description;
}