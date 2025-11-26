package com.atlasculinary.dtos;

import com.atlasculinary.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAccountStatusRequest {
    @NotNull(message = "Status is required")
    private AccountStatus status;
}
