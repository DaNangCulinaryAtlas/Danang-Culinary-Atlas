package com.atlasculinary.dtos;

import com.atlasculinary.enums.LicenseType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AddBusinessLicenseRequest {

    @NotNull(message = "License type is required")
    private LicenseType licenseType;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @Future(message = "Expire date must be in the future")
    private LocalDate expireDate;

    @NotBlank(message = "Document URL is required")
    private String documentUrl;
}