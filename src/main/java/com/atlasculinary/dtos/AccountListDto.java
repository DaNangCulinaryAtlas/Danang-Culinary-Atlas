package com.atlasculinary.dtos;

import com.atlasculinary.enums.AccountStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AccountListDto {
    private UUID accountId;
    private String email;
    private String fullName;
    private String avatarUrl;
    private AccountStatus status;
    private String role; // USER or VENDOR
    private LocalDateTime createdAt;
}
