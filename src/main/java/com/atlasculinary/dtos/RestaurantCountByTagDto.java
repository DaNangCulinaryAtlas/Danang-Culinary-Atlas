package com.atlasculinary.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantCountByTagDto {
    private Long tagId;
    private String tagName;
    private Long restaurantCount;
}
