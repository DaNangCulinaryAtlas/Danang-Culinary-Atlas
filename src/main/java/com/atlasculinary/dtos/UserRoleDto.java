package com.atlasculinary.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleDto {
    private Long roleId;
    private String roleName;
    private String description;

    private Boolean licensed;
}