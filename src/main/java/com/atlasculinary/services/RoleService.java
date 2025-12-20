package com.atlasculinary.services;

import com.atlasculinary.dtos.AddRoleRequest;
import com.atlasculinary.dtos.RoleDto;
import com.atlasculinary.dtos.UpdateRoleRequest;
import java.util.List;

public interface RoleService {
    List<RoleDto> getAllRoles();
    RoleDto createRole(AddRoleRequest request);
    RoleDto updateRole(Long roleId, UpdateRoleRequest request);
    void deleteRole(Long roleId);
}