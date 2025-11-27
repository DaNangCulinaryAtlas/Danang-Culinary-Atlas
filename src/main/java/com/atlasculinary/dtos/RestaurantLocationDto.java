package com.atlasculinary.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantLocationDto {
    private UUID restaurantId;
    private String name;
    private String address;
}
