package com.atlasculinary.services;

import com.atlasculinary.dtos.AddRestaurantRequest;
import com.atlasculinary.dtos.RestaurantDto;
import com.atlasculinary.dtos.RestaurantMapViewDto;
import com.atlasculinary.dtos.UpdateApprovalStatusRequest;
import com.atlasculinary.dtos.UpdateRestaurantRequest;
import com.atlasculinary.enums.ApprovalStatus;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface RestaurantService {

    RestaurantDto createRestaurant(UUID ownerAccountId, AddRestaurantRequest request);


    RestaurantDto getRestaurantById(UUID restaurantId);


    Page<RestaurantDto> getAllRestaurants(int page, int size, String sortBy, String sortDirection);

    Page<RestaurantDto> searchApprovedRestaurants(
            int page,
            int size,
            String sortBy,
            String sortDirection,
            List<Long> cuisineID,         // Lọc theo ID Loại Quán Ăn
            BigDecimal minRating,         // Lọc theo Rating Tối thiểu
            BigDecimal maxRating          // Lọc theo Rating Tối đa
    );
    Page<RestaurantDto> searchApprovedRestaurantsByName(
            int page,
            int size,
            String sortBy,
            String sortDirection,
            String restaurantName
    );

    Page<RestaurantDto> getAllRestaurantsByVendor(UUID vendorId, int page, int size, String sortBy, String sortDirection);


    RestaurantDto updateRestaurant(UUID restaurantId, UpdateRestaurantRequest request, UUID accessAccountId);


    void deleteRestaurant(UUID restaurantId, UUID accessAccountId);


    RestaurantDto updateApprovalStatus(
            UUID adminAccountId,
            UUID restaurantId,
            UpdateApprovalStatusRequest request
    );

    Page<RestaurantDto> getAllRestaurantsApproved(int page, int size, String sortBy, String sortDirection);

    List<RestaurantMapViewDto> getRestaurantsInMapView(int zoomLevel, BigDecimal minLat, BigDecimal maxLat, BigDecimal minLong, BigDecimal maxLong);
}
