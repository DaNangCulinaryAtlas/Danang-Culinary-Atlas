package com.atlasculinary.services;

import com.atlasculinary.dtos.*;
import com.atlasculinary.securities.CustomAccountDetails;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface DishService {

    DishDto createDish(AddDishRequest request, UUID accessAccountId);

    DishDto updateDish(UUID dishId, UpdateDishRequest request, UUID accessAccountId);

    DishDto updateDishStatus(UUID dishId, UpdateDishStatusRequest request, UUID accessAccountId);

    DishDto getDishById(UUID dishId);
    DishDto getDishDetailsForManagement(UUID dishId, CustomAccountDetails principal);

    Page<DishDto> getRestaurantDishes(UUID restaurantId, int page, int size, String sortBy, String sortDirection, UUID accessAccountId);;

    Page<DishDto> getAvailableDishes(UUID restaurantId, int page, int size, String sortBy, String sortDirection);

    Page<DishDto> searchDishes(
            List<String> tagNames,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            int page,
            int size,
            String sortBy,
            String sortOrder
    );
}