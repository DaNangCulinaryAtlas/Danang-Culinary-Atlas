package com.atlasculinary.services;

import com.atlasculinary.dtos.AccountDetailDto;
import com.atlasculinary.dtos.AccountListDto;
import com.atlasculinary.dtos.UpdateAccountStatusRequest;
import com.atlasculinary.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserManagementService {
    /**
     * Get paginated list of users
     */
    Page<AccountListDto> getUsers(AccountStatus status, String search, Pageable pageable);
    
    /**
     * Get paginated list of vendors
     */
    Page<AccountListDto> getVendors(AccountStatus status, String search, Pageable pageable);
    
    /**
     * Get all accounts (users, vendors, admins) with pagination and filters
     */
    Page<AccountListDto> getAllAccounts(AccountStatus status, String role, Pageable pageable);
    
    /**
     * Search accounts by keyword (email or fullName)
     */
    Page<AccountListDto> searchAccounts(String search, Pageable pageable);
    
    /**
     * Get account detail by ID
     */
    AccountDetailDto getAccountDetail(UUID accountId);
    
    /**
     * Update account status
     */
    void updateAccountStatus(UUID accountId, UpdateAccountStatusRequest request);
    
    /**
     * Send email to account
     */
    void sendEmailToAccount(UUID accountId, String subject, String content);
}
