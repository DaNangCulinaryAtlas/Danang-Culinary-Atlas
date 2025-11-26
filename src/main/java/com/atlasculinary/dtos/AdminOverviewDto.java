package com.atlasculinary.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminOverviewDto {
    private Long totalUserAccounts;
    private Long totalVendorAccounts;
    private Long totalApprovedRestaurants;
    private Long totalPendingRestaurants;
}
