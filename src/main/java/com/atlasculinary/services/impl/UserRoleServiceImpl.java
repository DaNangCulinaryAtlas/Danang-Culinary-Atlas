package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.AssignRoleRequest;
import com.atlasculinary.dtos.UserRoleDto;
import com.atlasculinary.entities.Account;
import com.atlasculinary.entities.AccountRoleMap;
import com.atlasculinary.entities.Role;
import com.atlasculinary.repositories.AccountRepository;
import com.atlasculinary.repositories.AccountRoleMapRepository;
import com.atlasculinary.repositories.RoleRepository;
import com.atlasculinary.services.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AccountRoleMapRepository accountRoleMapRepository;

    private static final Long ROLE_SUPER_ADMIN_ID = 4L;

    @Override
    public List<UserRoleDto> getUserRoles(UUID userId) {
        if (!accountRepository.existsById(userId)) {
            throw new RuntimeException("User không tồn tại");
        }

        return accountRoleMapRepository.findByAccountId(userId).stream()
                .map(map -> new UserRoleDto(
                        map.getRoleId(),
                        map.getRole().getRoleName(),
                        map.getRole().getDescription(),
                        map.getLicensed()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignRoleToUser(UUID userId, AssignRoleRequest request) {
        if (ROLE_SUPER_ADMIN_ID.equals(request.getRoleId())) {
            throw new RuntimeException("Không được phép gán quyền SUPER_ADMIN thông qua API này.");
        }

        if (accountRoleMapRepository.existsByAccountIdAndRoleId(userId, request.getRoleId())) {
            throw new RuntimeException("User đã sở hữu Role này rồi");
        }

        Account account = accountRepository.getReferenceById(userId);
        Role role = roleRepository.getReferenceById(request.getRoleId());

        AccountRoleMap map = new AccountRoleMap();
        map.setAccountId(userId);
        map.setRoleId(request.getRoleId());
        map.setAccount(account);
        map.setRole(role);

        map.setLicensed(false);

        accountRoleMapRepository.save(map);
    }

    @Override
    @Transactional
    public void revokeRoleFromUser(UUID userId, Long roleId) {

        if (ROLE_SUPER_ADMIN_ID.equals(roleId)) {
            throw new RuntimeException("Không được phép gỡ bỏ quyền SUPER_ADMIN.");
        }

        AccountRoleMap map = accountRoleMapRepository.findByAccountIdAndRoleId(userId, roleId)
                .orElseThrow(() -> new RuntimeException("User không sở hữu Role này"));

        accountRoleMapRepository.delete(map);
    }

    @Override
    @Transactional
    public void toggleRoleStatus(UUID userId, Long roleId, boolean licensed) {

        if (ROLE_SUPER_ADMIN_ID.equals(roleId)) {
            throw new RuntimeException("Không được phép thay đổi trạng thái của SUPER_ADMIN.");
        }

        AccountRoleMap map = accountRoleMapRepository.findByAccountIdAndRoleId(userId, roleId)
                .orElseThrow(() -> new RuntimeException("User không sở hữu Role này"));

        map.setLicensed(licensed);
        accountRoleMapRepository.save(map);
    }
}