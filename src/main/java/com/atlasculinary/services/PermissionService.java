package com.atlasculinary.services;

import com.atlasculinary.dtos.ActionDto;
import com.atlasculinary.dtos.RoleDto;
import com.atlasculinary.dtos.RolePermissionDto;
import com.atlasculinary.dtos.UpdateRolePermissionRequest;

import java.util.List;

public interface PermissionService {
    
    /**
     * Get all available actions in the system
     */
    List<ActionDto> getAllActions();
    
    /**
     * Get all roles in the system
     */
    List<RoleDto> getAllRoles();
    
    /**
     * Get all roles with their assigned permissions
     */
    List<RolePermissionDto> getAllRolesWithPermissions();
    
    /**
     * Get permissions for a specific role
     */
    RolePermissionDto getRolePermissions(Long roleId);
    
    /**
     * Update permissions for a specific role
     */
    RolePermissionDto updateRolePermissions(UpdateRolePermissionRequest request);
}
