package com.atlasculinary.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionDto {
    private Long roleId;
    private String roleName;
    private String description;
    private List<ActionDto> actions;
}
