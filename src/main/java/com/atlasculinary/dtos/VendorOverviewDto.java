package com.atlasculinary.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorOverviewDto {
    private Long totalRestaurants;
    private Long totalDishes;
    private Long totalReviews;
}
