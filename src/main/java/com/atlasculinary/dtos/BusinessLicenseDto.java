package com.atlasculinary.dtos;

import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.LicenseType;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BusinessLicenseDto {
    private UUID licenseId;

    private UUID restaurantId;
    private String restaurantName;

    private UUID ownerAccountId;
    private String ownerEmail;

    private UUID approvedByAccountId;
    private String approvedByEmail;

    private LicenseType licenseType;
    private String licenseNumber;
    private LocalDate issueDate;
    private LocalDate expireDate;
    private String documentUrl;

    private ApprovalStatus approvalStatus;
    private LocalDateTime approvedAt;
    private String rejectionReason;
}