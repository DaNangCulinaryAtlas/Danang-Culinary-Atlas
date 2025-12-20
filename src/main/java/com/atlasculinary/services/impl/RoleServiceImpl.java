package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.AddRoleRequest;
import com.atlasculinary.dtos.RoleDto;
import com.atlasculinary.dtos.UpdateRoleRequest;
import com.atlasculinary.entities.Role;
import com.atlasculinary.mappers.RoleMapper;
import com.atlasculinary.repositories.AccountRoleMapRepository;
import com.atlasculinary.repositories.RoleRepository;
import com.atlasculinary.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final AccountRoleMapRepository accountRoleMapRepository;
    private final RoleMapper roleMapper;

    private static final Long SUPER_ADMIN_ROLE_ID = 4L;
    private static final String SUPER_ADMIN_ROLE_NAME = "SUPER_ADMIN";

    @Override
    public List<RoleDto> getAllRoles() {
        var roleList = roleRepository.findAll();
        return roleMapper.toDtoList(roleList);
    }

    @Override
    @Transactional
    public RoleDto createRole(AddRoleRequest request) {
        if (SUPER_ADMIN_ROLE_NAME.equalsIgnoreCase(request.getRoleName())) {
            throw new RuntimeException("Không thể tạo Role với tên dành riêng: " + SUPER_ADMIN_ROLE_NAME);
        }

        if (roleRepository.existsByRoleName(request.getRoleName())) {
            throw new RuntimeException("Tên Role đã tồn tại");
        }

        Role role = roleMapper.toEntity(request);
        return roleMapper.toDto(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleDto updateRole(Long roleId, UpdateRoleRequest request) {
        if (SUPER_ADMIN_ROLE_ID.equals(roleId)) {
            throw new RuntimeException("Không được phép chỉnh sửa Role SUPER_ADMIN.");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Role"));

        roleMapper.updateEntityFromRequest(request, role);

        return roleMapper.toDto(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {

        if (SUPER_ADMIN_ROLE_ID.equals(roleId)) {
            throw new RuntimeException("Hành động bị TỪ CHỐI: Không thể xóa Role SUPER_ADMIN.");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Role với ID: " + roleId));

        boolean isInUse = accountRoleMapRepository.existsByRoleId(roleId);
        if (isInUse) {
            throw new RuntimeException("Không thể xóa Role này vì đang có người dùng sử dụng.");
        }

        roleRepository.delete(role);
    }
}