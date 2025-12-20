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
     * Update permissions (assign/remove actions) for a specific role.
     * Note: New permissions will default to requiresLicense = true (Safe mode).
     */
    RolePermissionDto updateRolePermissions(UpdateRolePermissionRequest request);

    /**
     * Update the sensitivity configuration (requiresLicense) for a specific Role-Action pair.
     * * @param roleId The ID of the role
     * @param actionId The ID of the action
     * @param requiresLicense true if the user needs to be licensed to use this action, false otherwise
     */
    void updateRoleActionConfig(Long roleId, Long actionId, boolean requiresLicense);
}