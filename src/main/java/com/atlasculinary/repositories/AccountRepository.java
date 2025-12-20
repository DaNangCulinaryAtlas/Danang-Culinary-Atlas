package com.atlasculinary.repositories;

import com.atlasculinary.entities.Account;
import com.atlasculinary.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  
  @Query("SELECT DISTINCT a FROM Account a " +
         "JOIN a.accountRoleMapSet arm " +
         "JOIN arm.role r " +
         "WHERE r.roleName = :roleName " +
         "AND (:status IS NULL OR a.status = :status) " +
         "AND (:search IS NULL OR :search = '' OR " +
         "LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(a.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
         "ORDER BY a.createdAt DESC")
  Page<Account> findAccountsByRole(@Param("roleName") String roleName, 
                                    @Param("status") AccountStatus status, 
                                    @Param("search") String search, 
                                    Pageable pageable);
  
  @Query("SELECT a FROM Account a WHERE a.status = :status ORDER BY a.createdAt DESC")
  Page<Account> findByStatus(@Param("status") AccountStatus status, Pageable pageable);
  
  @Query("SELECT DISTINCT a FROM Account a " +
         "LEFT JOIN a.accountRoleMapSet arm " +
         "LEFT JOIN arm.role r " +
         "WHERE (:status IS NULL OR a.status = :status) " +
         "AND (:roleName IS NULL OR :roleName = '' OR r.roleName = :roleName) " +
         "AND NOT EXISTS (SELECT 1 FROM AccountRoleMap arm2 JOIN arm2.role r2 " +
         "WHERE arm2.account = a AND r2.roleName = 'SUPER_ADMIN') " +
         "ORDER BY a.createdAt DESC")
  Page<Account> findAllWithFilters(@Param("status") AccountStatus status,
                                    @Param("roleName") String roleName,
                                    Pageable pageable);
}
