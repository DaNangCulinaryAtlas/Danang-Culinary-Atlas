package com.atlasculinary.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionDto {
    private Long actionId;
    private String actionCode;
    private String actionName;
    private Boolean requiresLicense;
}