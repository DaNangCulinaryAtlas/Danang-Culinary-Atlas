package com.atlasculinary.dtos;

import com.atlasculinary.enums.AccountStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AccountDetailDto {
    private UUID accountId;
    private String email;
    private String fullName;
    private String avatarUrl;
    private AccountStatus status;
    private String role; // USER or VENDOR
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Profile details
    private LocalDate dob;
    private String gender;
    private String phone;
    
    // Vendor specific
    private String description;
    
    // Statistics
    private Long totalRestaurants; // For vendors
    private Long totalReviews; // For users
}
