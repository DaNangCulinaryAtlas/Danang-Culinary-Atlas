package com.atlasculinary.repositories;

import com.atlasculinary.entities.AccountRoleMap;
import com.atlasculinary.entities.AccountRoleMapId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRoleMapRepository extends JpaRepository<AccountRoleMap, AccountRoleMapId> {
    
    @Query("SELECT arm FROM AccountRoleMap arm " +
           "JOIN FETCH arm.role r " +
           "WHERE arm.accountId = :accountId")
    List<AccountRoleMap> findByAccountIdWithRole(@Param("accountId") UUID accountId);
    Optional<AccountRoleMap> findByAccountIdAndRoleId(UUID accountId, Long roleId);

    boolean existsByAccountIdAndRoleId(UUID accountId, Long roleId);

    List<AccountRoleMap> findByAccountId(UUID accountId);

    boolean existsByRoleId(Long roleId);
}
