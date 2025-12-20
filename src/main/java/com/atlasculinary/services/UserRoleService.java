package com.atlasculinary.services;

import com.atlasculinary.dtos.AssignRoleRequest;
import com.atlasculinary.dtos.UserRoleDto;

import java.util.List;
import java.util.UUID;

public interface UserRoleService {
    // Xem Role của User
    List<UserRoleDto> getUserRoles(UUID userId);

    // Gán Role (Assign Role)
    void assignRoleToUser(UUID userId, AssignRoleRequest request);

    // Gỡ Role (Revoke Role)
    void revokeRoleFromUser(UUID userId, Long roleId);

    // Duyệt / Khóa Role (Toggle Status)
    void toggleRoleStatus(UUID userId, Long roleId, boolean isActive);
}