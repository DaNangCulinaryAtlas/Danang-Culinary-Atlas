package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.ActionDto;
import com.atlasculinary.dtos.RoleDto;
import com.atlasculinary.dtos.RolePermissionDto;
import com.atlasculinary.dtos.UpdateRolePermissionRequest;
import com.atlasculinary.entities.Action;
import com.atlasculinary.entities.Role;
import com.atlasculinary.entities.RoleActionMap;
import com.atlasculinary.exceptions.ResourceNotFoundException;
import com.atlasculinary.exceptions.InvalidRequestException;
import com.atlasculinary.mappers.ActionMapper;
import com.atlasculinary.mappers.RoleMapper;
import com.atlasculinary.repositories.ActionRepository;
import com.atlasculinary.repositories.RoleActionMapRepository;
import com.atlasculinary.repositories.RoleRepository;
import com.atlasculinary.services.PermissionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final RoleRepository roleRepository;
    private final ActionRepository actionRepository;
    private final RoleActionMapRepository roleActionMapRepository;
    private final ActionMapper actionMapper;
    private final RoleMapper roleMapper;

    @Override
    public List<ActionDto> getAllActions() {
        return actionRepository.findAll().stream()
                .map(actionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RolePermissionDto> getAllRolesWithPermissions() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::mapToRolePermissionDto)
                .collect(Collectors.toList());
    }

    @Override
    public RolePermissionDto getRolePermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        
        return mapToRolePermissionDto(role);
    }

    @Override
    @Transactional
    public RolePermissionDto updateRolePermissions(UpdateRolePermissionRequest request) {
        // Validate role exists
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + request.getRoleId()));
        
        // Validate all action IDs exist
        List<Action> actions = actionRepository.findAllById(request.getActionIds());
        if (actions.size() != request.getActionIds().size()) {
            throw new InvalidRequestException("Some action IDs are invalid");
        }
        
        // Delete existing permissions for this role
        roleActionMapRepository.deleteByRoleId(request.getRoleId());
        
        // Create new permission mappings
        List<RoleActionMap> newMappings = actions.stream()
                .map(action -> {
                    RoleActionMap mapping = new RoleActionMap();
                    mapping.setRoleId(role.getRoleId());
                    mapping.setActionId(action.getActionId());
                    mapping.setRole(role);
                    mapping.setAction(action);
                    return mapping;
                })
                .collect(Collectors.toList());
        
        roleActionMapRepository.saveAll(newMappings);
        
        // Return updated permissions
        return getRolePermissions(request.getRoleId());
    }

    private RolePermissionDto mapToRolePermissionDto(Role role) {
        RolePermissionDto dto = new RolePermissionDto();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());
        dto.setDescription(role.getDescription());
        
        // Get all actions for this role
        List<RoleActionMap> roleMappings = roleActionMapRepository.findByRoleId(role.getRoleId());
        List<ActionDto> actionDtos = roleMappings.stream()
                .map(mapping -> actionMapper.toDto(mapping.getAction()))
                .collect(Collectors.toList());
        
        dto.setActions(actionDtos);
        return dto;
    }
}
