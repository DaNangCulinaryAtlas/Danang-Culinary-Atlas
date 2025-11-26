package com.atlasculinary.repositories;

import com.atlasculinary.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
  boolean existsByEmail(String email);
  Optional<Account> findByEmail(String email);
  
  @Query("SELECT COUNT(a) FROM Account a JOIN a.accountRoleMapSet arm JOIN arm.role r WHERE r.roleName = :roleName")
  Long countByRoleName(@Param("roleName") String roleName);
}