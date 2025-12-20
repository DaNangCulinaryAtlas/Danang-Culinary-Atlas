package com.atlasculinary.repositories;

import com.atlasculinary.entities.RoleActionMap;
import com.atlasculinary.entities.RoleActionMapId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleActionMapRepository extends JpaRepository<RoleActionMap, RoleActionMapId> {

    /**
     * Lấy danh sách quyền của Role kèm theo thông tin Action (JOIN FETCH).
     */
    @Query("SELECT ram FROM RoleActionMap ram JOIN FETCH ram.action WHERE ram.roleId = :roleId")
    List<RoleActionMap> findByRoleIdWithAction(@Param("roleId") Long roleId);

    /**
     * Dùng cho API Admin cập nhật cấu hình (requiresLicense) cho một quyền cụ thể.
     */
    @Query("SELECT ram FROM RoleActionMap ram WHERE ram.roleId = :roleId AND ram.actionId = :actionId")
    Optional<RoleActionMap> findByRoleIdAndActionId(@Param("roleId") Long roleId, @Param("actionId") Long actionId);

    /**
     * Dùng khi cập nhật lại toàn bộ quyền cho Role (Xóa hết cũ đi gán mới).
     */
    @Modifying
    @Query("DELETE FROM RoleActionMap ram WHERE ram.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

}