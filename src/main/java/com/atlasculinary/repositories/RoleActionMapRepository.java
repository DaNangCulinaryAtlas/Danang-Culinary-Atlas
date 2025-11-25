package com.atlasculinary.repositories;

import com.atlasculinary.entities.RoleActionMap;
import com.atlasculinary.entities.RoleActionMapId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleActionMapRepository extends JpaRepository<RoleActionMap, RoleActionMapId> {

    @Query("SELECT ram.action.actionCode FROM RoleActionMap ram WHERE ram.roleId = :roleId")
    List<String> findActionCodesByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT ram FROM RoleActionMap ram WHERE ram.roleId = :roleId")
    List<RoleActionMap> findByRoleId(@Param("roleId") Long roleId);
    
    @Modifying
    @Query("DELETE FROM RoleActionMap ram WHERE ram.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);
}
