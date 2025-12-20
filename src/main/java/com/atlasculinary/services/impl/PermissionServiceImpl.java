package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.*;
import com.atlasculinary.entities.Action;
import com.atlasculinary.entities.Role;
import com.atlasculinary.entities.RoleActionMap;
import com.atlasculinary.exceptions.InvalidRequestException;
import com.atlasculinary.exceptions.ResourceNotFoundException;
import com.atlasculinary.mappers.ActionMapper;
import com.atlasculinary.mappers.RoleMapper;
import com.atlasculinary.mappers.RolePermissionMapper; // Import Mapper mới
import com.atlasculinary.repositories.ActionRepository;
import com.atlasculinary.repositories.RoleActionMapRepository;
import com.atlasculinary.repositories.RoleRepository;
import com.atlasculinary.services.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final RolePermissionMapper rolePermissionMapper;

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
                .map(role -> {
                    // Gọi repository lấy list maps (đã join fetch action)
                    List<RoleActionMap> maps = roleActionMapRepository.findByRoleIdWithAction(role.getRoleId());
                    // Dùng Mapper để gộp Role + Maps thành DTO
                    return rolePermissionMapper.toDto(role, maps);
                })
                .collect(Collectors.toList());
    }

    @Override
    public RolePermissionDto getRolePermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        // Lấy danh sách Action kèm cấu hình requiresLicense
        List<RoleActionMap> maps = roleActionMapRepository.findByRoleIdWithAction(roleId);

        // Map sang DTO
        return rolePermissionMapper.toDto(role, maps);
    }

    @Override
    @Transactional
    public RolePermissionDto updateRolePermissions(UpdateRolePermissionRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + request.getRoleId()));

        List<Action> actions = actionRepository.findAllById(request.getActionIds());
        if (actions.size() != request.getActionIds().size()) {
            throw new InvalidRequestException("Some action IDs are invalid");
        }

        // 1. Xóa mapping cũ
        roleActionMapRepository.deleteByRoleId(request.getRoleId());

        // 2. Tạo mapping mới
        List<RoleActionMap> newMappings = actions.stream()
                .map(action -> {
                    RoleActionMap mapping = new RoleActionMap();
                    mapping.setRoleId(role.getRoleId());
                    mapping.setActionId(action.getActionId());
                    mapping.setRole(role);
                    mapping.setAction(action);

                    // MẶC ĐỊNH: Khi gán quyền mới, set requiresLicense = TRUE (An toàn)
                    // Admin sẽ vào cấu hình lại sau nếu muốn mở (set false)
                    mapping.setRequiresLicense(true);

                    return mapping;
                })
                .collect(Collectors.toList());

        roleActionMapRepository.saveAll(newMappings);

        // 3. Trả về kết quả mới nhất
        return getRolePermissions(request.getRoleId());
    }

    @Override
    @Transactional
    public void updateRoleActionConfig(Long roleId, Long actionId, boolean requiresLicense) {
        RoleActionMap map = roleActionMapRepository.findByRoleIdAndActionId(roleId, actionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found for this role"));

        map.setRequiresLicense(requiresLicense);
        roleActionMapRepository.save(map);
    }
}