package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.AccountDetailDto;
import com.atlasculinary.dtos.AccountListDto;
import com.atlasculinary.dtos.UpdateAccountStatusRequest;
import com.atlasculinary.entities.*;
import com.atlasculinary.enums.AccountStatus;
import com.atlasculinary.exceptions.ResourceNotFoundException;
import com.atlasculinary.repositories.*;
import com.atlasculinary.services.EmailService;
import com.atlasculinary.services.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {
    
    private final AccountRepository accountRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public Page<AccountListDto> getUsers(AccountStatus status, String search, Pageable pageable) {
        Page<Account> accounts = accountRepository.findAccountsByRole("USER", status, search, pageable);
        return accounts.map(this::mapToAccountListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountListDto> getVendors(AccountStatus status, String search, Pageable pageable) {
        Page<Account> accounts = accountRepository.findAccountsByRole("VENDOR", status, search, pageable);
        return accounts.map(this::mapToAccountListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountListDto> getAllAccounts(AccountStatus status, String role, Pageable pageable) {
        Page<Account> accounts = accountRepository.findAllWithFilters(status, role, pageable);
        return accounts.map(this::mapToAccountListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDetailDto getAccountDetail(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        
        AccountDetailDto dto = new AccountDetailDto();
        dto.setAccountId(account.getAccountId());
        dto.setEmail(account.getEmail());
        dto.setFullName(account.getFullName());
        dto.setAvatarUrl(account.getAvatarUrl());
        dto.setStatus(account.getStatus());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        
        // Get role
        String role = account.getAccountRoleMapSet().stream()
                .map(arm -> arm.getRole().getRoleName())
                .filter(r -> r.equals("USER") || r.equals("VENDOR"))
                .findFirst()
                .orElse(null);
        dto.setRole(role);
        
        // Get profile details
        if ("USER".equals(role) && account.getUserProfile() != null) {
            UserProfile profile = account.getUserProfile();
            dto.setDob(profile.getDob());
            dto.setGender(profile.getGender() != null ? profile.getGender().name() : null);
            
            // Get review count
            Long reviewCount = reviewRepository.countByReviewerAccount(account);
            dto.setTotalReviews(reviewCount);
            
        } else if ("VENDOR".equals(role) && account.getVendorProfile() != null) {
            VendorProfile profile = account.getVendorProfile();
            dto.setDob(profile.getDob());
            dto.setGender(profile.getGender() != null ? profile.getGender().name() : null);
            dto.setPhone(profile.getPhone());
            dto.setDescription(profile.getDescription());
            
            // Get restaurant count
            Long restaurantCount = restaurantRepository.countByOwnerAccount(account);
            dto.setTotalRestaurants(restaurantCount);
        }
        
        return dto;
    }

    @Override
    @Transactional
    public void updateAccountStatus(UUID accountId, UpdateAccountStatusRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        
        AccountStatus oldStatus = account.getStatus();
        account.setStatus(request.getStatus());
        accountRepository.save(account);
        
        log.info("Updated account {} status from {} to {}", accountId, oldStatus, request.getStatus());
    }

    @Override
    @Transactional
    public void sendEmailToAccount(UUID accountId, String subject, String content) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        
        emailService.sendEmail(account.getEmail(), subject, content);
        log.info("Sent email to account {} with subject: {}", accountId, subject);
    }
    
    private AccountListDto mapToAccountListDto(Account account) {
        AccountListDto dto = new AccountListDto();
        dto.setAccountId(account.getAccountId());
        dto.setEmail(account.getEmail());
        dto.setFullName(account.getFullName());
        dto.setAvatarUrl(account.getAvatarUrl());
        dto.setStatus(account.getStatus());
        dto.setCreatedAt(account.getCreatedAt());
        
        // Get role (exclude SUPER_ADMIN)
        String role = account.getAccountRoleMapSet().stream()
                .map(arm -> arm.getRole().getRoleName())
                .filter(r -> !r.equals("SUPER_ADMIN"))
                .findFirst()
                .orElse(null);
        dto.setRole(role);
        
        return dto;
    }
}
