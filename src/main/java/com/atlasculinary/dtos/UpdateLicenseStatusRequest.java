package com.atlasculinary.dtos;

import com.atlasculinary.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLicenseStatusRequest {
    @NotNull(message = "Approval status is required")
    private ApprovalStatus status;

    private String rejectionReason;
}