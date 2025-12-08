package com.atlasculinary.controllers;

import com.atlasculinary.dtos.AddRestaurantRequest;
import com.atlasculinary.dtos.RestaurantDto;
import com.atlasculinary.dtos.RestaurantMapViewDto;
import com.atlasculinary.dtos.UpdateApprovalStatusRequest;
import com.atlasculinary.dtos.UpdateRestaurantRequest;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.securities.CustomAccountDetails;
import com.atlasculinary.services.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Tag(name = "Restaurant Management", description = "API for managing restaurant data")
public class RestaurantController {

    private final RestaurantService restaurantService;


    // =================================================================
    // === VENDOR/ADMIN ENDPOINTS (CRUD) ===
    // =================================================================

    @Operation(summary = "Create a new restaurant (by Vendor or Admin)")
    @PreAuthorize("hasAuthority('RESTAURANT_CREATE')")
    @PostMapping("/restaurants")
    public ResponseEntity<RestaurantDto> createRestaurant(
            @RequestBody @Valid AddRestaurantRequest addRestaurantRequest,
            @AuthenticationPrincipal CustomAccountDetails principal) {
        var ownerAccountId = principal.getAccountId();
        // Service sẽ gán principal.accountId làm Owner của nhà hàng
        var restaurantDto = restaurantService.createRestaurant(ownerAccountId, addRestaurantRequest);
        return new ResponseEntity<>(restaurantDto,HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing restaurant's details")
    @PatchMapping("/restaurants/{restaurantId}")
    @PreAuthorize("hasAuthority('RESTAURANT_UPDATE')")
    ResponseEntity<RestaurantDto> updateRestaurant(@PathVariable UUID restaurantId,
                                                   @Valid @RequestBody UpdateRestaurantRequest updateRestaurantRequest,
                                                   @AuthenticationPrincipal CustomAccountDetails principal) {
        var accessAccountId = principal.getAccountId();
        // Service kiểm tra accessAccountId có quyền sở hữu/Admin
        var restaurantDto = restaurantService.updateRestaurant(restaurantId, updateRestaurantRequest, accessAccountId);
        return ResponseEntity.ok(restaurantDto);
    }

    @Operation(summary = "Delete a restaurant by Id (Soft Delete, chỉ Owner hoặc Admin)")
    @DeleteMapping("/restaurants/{restaurantId}")
    @PreAuthorize("hasAuthority('RESTAURANT_DELETE')")
    ResponseEntity<Void> deleteRestaurant(@PathVariable UUID restaurantId,
                                          @AuthenticationPrincipal CustomAccountDetails principal) {
        var accessAccountId = principal.getAccountId();
        // Cải tiến: Truyền accessAccountId vào Service để kiểm tra quyền sở hữu/Admin
        restaurantService.deleteRestaurant(restaurantId, accessAccountId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // =================================================================
    // === PUBLIC ENDPOINTS (READ) ===
    // =================================================================

    @Operation(summary = "Get restaurant details by Id (Public)")
    @GetMapping("/restaurants/{restaurantId}")
    ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable UUID restaurantId) {
        // Service chỉ trả về nhà hàng đã APPROVED và không bị xóa
        var restaurantDto = restaurantService.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurantDto);
    }

    @Operation(summary = "Get all approved restaurants with pagination and sorting")
    @GetMapping("/restaurants")
    public ResponseEntity<Page<RestaurantDto>> getAllRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        // Service chỉ trả về các nhà hàng đã APPROVED
        Page<RestaurantDto> restaurantsPage = restaurantService.getAllRestaurantsApproved(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(restaurantsPage);
    }

    @Operation(summary = "Search approved restaurants by name with pagination and sorting")
    @GetMapping("/restaurants/name")
    public ResponseEntity<Page<RestaurantDto>> searchApprovedRestaurantsByName(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam String name) {

        Page<RestaurantDto> restaurantsPage = restaurantService
                .searchApprovedRestaurantsByName(page, size, sortBy, sortDirection, name);

        return ResponseEntity.ok(restaurantsPage);
    }

    @Operation(summary = "Search restaurants by dish name with optional approval status filter")
    @GetMapping("/restaurants/by-dish")
    public ResponseEntity<Page<RestaurantDto>> searchRestaurantsByDishName(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam String dishName,
            @RequestParam(required = false) ApprovalStatus status) {

        Page<RestaurantDto> restaurants = restaurantService.searchRestaurantsByDishName(page, size, sortBy, sortDirection, dishName, status);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("restaurants/search")
    public ResponseEntity<Page<RestaurantDto>> searchRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "average_rating") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) List<Long> cuisineID,
            @RequestParam(defaultValue = "0.0") BigDecimal minRating,
            @RequestParam(defaultValue = "5.0") BigDecimal maxRating)
    {

        Page<RestaurantDto> resultPage = restaurantService.searchApprovedRestaurants(
                page,
                size,
                sortBy,
                sortDirection,
                cuisineID,
                minRating,
                maxRating
        );

        return ResponseEntity.ok(resultPage);
    }

    @GetMapping("/restaurants/map-view")
    public ResponseEntity<List<RestaurantMapViewDto>> getRestaurantsInMapView(
            @RequestParam(defaultValue = "15") int zoomLevel,
            @RequestParam(required = false) BigDecimal minLat,
            @RequestParam(required = false) BigDecimal maxLat,
            @RequestParam(required = false) BigDecimal minLng,
            @RequestParam(required = false) BigDecimal maxLng)
    {
        BigDecimal defaultMinLat = minLat != null ? minLat : new BigDecimal("-90.0");
        BigDecimal defaultMaxLat = maxLat != null ? maxLat : new BigDecimal("90.0");
        BigDecimal defaultMinLng = minLng != null ? minLng : new BigDecimal("-180.0");
        BigDecimal defaultMaxLng = maxLng != null ? maxLng : new BigDecimal("180.0");

        List<RestaurantMapViewDto> restaurantDtoList =  restaurantService.getRestaurantsInMapView(
                zoomLevel,
                defaultMinLat,
                defaultMaxLat,
                defaultMinLng,
                defaultMaxLng
        );
        return ResponseEntity.ok(restaurantDtoList);

    }

    // =================================================================
    // === VENDOR OWNERSHIP ENDPOINTS ===
    // =================================================================

    @Operation(summary = "Get all restaurants belonging to a specific Vendor (dành cho Vendor/Admin)")
    @GetMapping("/vendors/{vendorId}/restaurants") // URI: /api/v1/vendors/{vendorId}/restaurants
    @PreAuthorize("hasAuthority('RESTAURANT_VIEW_OWN') or (hasAuthority('RESTAURANT_VIEW_ALL') and hasAuthority('ROLE_ADMIN'))")
    public ResponseEntity<Page<RestaurantDto>> getAllRestaurantsByVendor(
            @PathVariable UUID vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @AuthenticationPrincipal CustomAccountDetails principal
    ) {
        // Service sẽ trả về danh sách đầy đủ (bao gồm PENDING, REJECTED) cho Vendor hoặc Admin
        Page<RestaurantDto> restaurantsPage = restaurantService.getAllRestaurantsByVendor(vendorId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(restaurantsPage);
    }


    // =================================================================
    // === ADMIN ENDPOINTS (APPROVAL) ===
    // =================================================================

    @Operation(summary = "Update the approval status of a restaurant (Admin only)")
    @PatchMapping("/admin/restaurants/{restaurantId}/approval") // URI: /api/v1/admin/restaurants/{id}/approval
    @PreAuthorize("hasAuthority('RESTAURANT_APPROVE') or hasAuthority('RESTAURANT_REJECT')")
    public ResponseEntity<RestaurantDto> updateApprovalStatus(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody UpdateApprovalStatusRequest request,
            @AuthenticationPrincipal CustomAccountDetails principal) {

        var adminAccountId = principal.getAccountId();
        RestaurantDto updatedRestaurant = restaurantService.updateApprovalStatus(adminAccountId, restaurantId, request);
        return ResponseEntity.ok(updatedRestaurant);
    }

    @Operation(summary = "ADMIN: Get all restaurants with any status (PENDING, APPROVED, REJECTED)")
    @GetMapping("/admin/restaurants") // URI: /api/v1/admin/restaurants
    @PreAuthorize("hasAuthority('RESTAURANT_VIEW_ALL')")
    public ResponseEntity<Page<RestaurantDto>> getAllRestaurantsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        // Service này sẽ không lọc theo trạng thái APPROVED
        Page<RestaurantDto> restaurantsPage = restaurantService.getAllRestaurants(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(restaurantsPage);
    }
}