package com.atlasculinary.dtos;

import jakarta.validation.constraints.Future;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateBusinessLicenseRequest {
    private String licenseNumber;
    private LocalDate issueDate;

    @Future
    private LocalDate expireDate;

    private String documentUrl;
}