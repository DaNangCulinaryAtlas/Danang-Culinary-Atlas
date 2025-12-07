package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.*;
import com.atlasculinary.entities.*;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.RestaurantStatus;
import com.atlasculinary.exceptions.InvalidRequestException;
import com.atlasculinary.exceptions.ResourceNotFoundException;
import com.atlasculinary.mappers.RestaurantMapper;
import com.atlasculinary.repositories.*;
import com.atlasculinary.services.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hibernate.metamodel.spi.ValueAccess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {
    private static final Logger LOGGER = Logger.getLogger(RestaurantServiceImpl.class.getName());
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTagService restaurantTagService;
    private final DishTagService dishTagService;
    private final AccountService accountService;
    private final WardRepository wardRepository;
    private final RestaurantMapper restaurantMapper;
    private final RestaurantStatsRepository restaurantStatsRepository;
    private final NotificationService notificationService;



    @Override
    @Transactional
    public RestaurantDto createRestaurant(UUID ownerAccountId, AddRestaurantRequest request) {
        var vendor = accountService.getAccountById(ownerAccountId);

        var ward = wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + request.getWardId()));

        Restaurant restaurant = restaurantMapper.toEntity(request);

        restaurant.setOwnerAccount(vendor);
        restaurant.setWard(ward);
        restaurant.setApprovalStatus(ApprovalStatus.PENDING);
        restaurant.setCreatedAt(LocalDateTime.now());
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        RestaurantStats newStats = new RestaurantStats();
        newStats.setRestaurant(savedRestaurant);
        restaurantStatsRepository.save(newStats);
        System.out.println("RestaurantId " + savedRestaurant.getRestaurantId() + ", TagIds");
        restaurantTagService.addTagsToRestaurant(savedRestaurant.getRestaurantId(), request.getTagIds());
        notificationService.notifyAdminNewRestaurantSubmission(savedRestaurant.getRestaurantId());
        return restaurantMapper.toDto(savedRestaurant);
    }

    @Override
    public RestaurantDto getRestaurantById(UUID restaurantId) {
        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        return restaurantMapper.toDto(restaurant);
    }

    @Override
    public Page<RestaurantDto> getAllRestaurants(int page, int size, String sortBy, String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC :
                Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Restaurant> restaurantPage = restaurantRepository.findAll(pageable);

        return restaurantPage.map(restaurantMapper::toDto);
    }

    @Override
    public Page<RestaurantDto> searchApprovedRestaurantsByName( int page, int size, String sortBy, String sortDirection, String restaurantName) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC :
                Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Restaurant> restaurantPage = restaurantRepository
                .findByApprovalStatusAndNameContainingIgnoreCase(
                        ApprovalStatus.APPROVED,
                        restaurantName,
                        pageable
                );
        return restaurantPage.map(restaurantMapper::toDto);
    }

    private String mapSortByColumn(String sortBy) {
        if ("average_rating".equalsIgnoreCase(sortBy)) {
            return "rs.average_rating";
        }
        return "r." + sortBy;
    }

    @Override
    public Page<RestaurantDto> searchApprovedRestaurants(int page, int size, String sortBy, String sortDirection, List<Long> cuisineID, BigDecimal minRating, BigDecimal maxRating)
    {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC :
                Sort.Direction.ASC;

        sortBy = mapSortByColumn(sortBy);
        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Restaurant> restaurantPage;
        if (cuisineID == null || cuisineID.isEmpty()) {
            restaurantPage = restaurantRepository.findApprovedRestaurantsWithoutTag(
                    minRating,
                    maxRating,
                    pageable
            );
        } else {
            restaurantPage = restaurantRepository.findApprovedRestaurantsByCriteria(
                    cuisineID,
                    minRating,
                    maxRating,
                    pageable
            );
        }


        return restaurantPage.map(restaurantMapper::toDto);
    }

    @Override
    public Page<RestaurantDto> searchRestaurantsByDishName(int page, int size, String sortBy, String sortDirection, String dishName) {
        if (dishName == null || dishName.trim().isEmpty()) {
            throw new InvalidRequestException("Dish name must not be empty.");
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC :
                Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Restaurant> restaurantPage = restaurantRepository.findApprovedRestaurantsByDishName(dishName.trim(), pageable);
        return restaurantPage.map(restaurantMapper::toDto);
    }


    @Override
    public Page<RestaurantDto> getAllRestaurantsByVendor(UUID vendorId, int page, int size, String sortBy, String sortDirection) {
        Account vendor = accountService.getAccountById(vendorId);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC :
                Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Restaurant> restaurantPage = restaurantRepository.findByOwnerAccount_AccountId(vendorId, pageable);

        return restaurantPage.map(restaurantMapper::toDto);
    }

    @Override
    @Transactional
    public RestaurantDto updateRestaurant(UUID restaurantId, UpdateRestaurantRequest request, UUID accessAccountId) {
        Account accessingAccount = accountService.getAccountById(accessAccountId);

        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        UUID ownerId = restaurant.getOwnerAccount().getAccountId();
        boolean isAdmin = accountService.isAdmin(accessAccountId);

        if (!ownerId.equals(accessAccountId) && !isAdmin) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa thông tin nhà hàng này.");
        }
        restaurantTagService.updateTagsForRestaurant(restaurantId, request.getTagIds());
        restaurantMapper.updateRestaurantFromRequest(request, restaurant);


        var requestWardId = request.getWardId();
        if (requestWardId != null) {
            Ward ward = wardRepository.findById(requestWardId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + requestWardId));

            restaurant.setWard(ward);
        }

        restaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.toDto(restaurant);

    }

    @Override
    @Transactional
    public void deleteRestaurant(UUID restaurantId, UUID accessAccountId) {
        if (!accountService.isExist(accessAccountId)) {
            throw new ResourceNotFoundException("Access Account not found with ID: " + accessAccountId);
        }

        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        UUID ownerId = restaurant.getOwnerAccount().getAccountId();
        boolean isAdmin = accountService.isAdmin(accessAccountId);

        if (!ownerId.equals(accessAccountId) && !isAdmin) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa thông tin nhà hàng này.");
        }

        // Soft delete: chỉ cập nhật trạng thái, không xoá cứng record
        restaurant.setStatus(RestaurantStatus.CLOSED);
        restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional
    public RestaurantDto updateApprovalStatus(UUID adminAccountId, UUID restaurantId, UpdateApprovalStatusRequest request) {

        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        var requestStatus = request.getStatus();

        if (requestStatus != restaurant.getApprovalStatus()) {

            restaurant.setApprovalStatus(requestStatus);
            restaurant.setApprovedAt(LocalDateTime.now());

            Account admin = accountService.getAccountById(adminAccountId);
            restaurant.setApprovedByAccount(admin);

            if (requestStatus == ApprovalStatus.REJECTED) {
                String rejectionReason = request.getRejectionReason();
                if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                    throw new InvalidRequestException("Rejection reason is mandatory when status is REJECTED.");
                }

                restaurant.setRejectionReason(rejectionReason);

            } else {
                restaurant.setRejectionReason(null);
            }

            restaurant = restaurantRepository.save(restaurant);
            LOGGER.severe("Cap Nhat Trang Thai " + restaurant.getApprovalStatus());
            notificationService.notifyVendorRestaurantStatusUpdate(new RestaurantStatusUpdateRequest(
                    restaurant.getOwnerAccount().getAccountId(),
                    restaurant.getName(),
                    restaurant.getApprovalStatus(),
                    restaurant.getRejectionReason()
            ));

        }

        return restaurantMapper.toDto(restaurant);
    }

    @Override
    public Page<RestaurantDto> getAllRestaurantsApproved(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC :
                Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Restaurant> restaurantPage = restaurantRepository.findAllByStatusAndApprovalStatus(pageable,
                RestaurantStatus.ACTIVE,
                ApprovalStatus.APPROVED);

        return restaurantPage.map(restaurantMapper::toDto);
    }

    private BigDecimal getMinRatingForZoom(int zoomLevel) {
        if (zoomLevel <= 10) {
            return new BigDecimal("4.5"); // 10 trở xuống: >= 4.5
        } else if (zoomLevel <= 13) {
            return new BigDecimal("4.0"); // 11-13: >= 4.0
        } else if (zoomLevel <= 15) {
            return new BigDecimal("3.5"); // 14-15: >= 3.5
        } else { // zoomLevel >= 16
            return new BigDecimal("0.0"); // 16 trở lên: Hiển thị tất cả
        }
    }

    @Override
    @Transactional
    public List<RestaurantMapViewDto> getRestaurantsInMapView(
            int zoomLevel,
            BigDecimal minLat,
            BigDecimal maxLat,
            BigDecimal minLng,
            BigDecimal maxLng)
    {
        if (zoomLevel <= 0) throw new InvalidRequestException("ZoomLevel not <= 0");
        BigDecimal minRating = getMinRatingForZoom(zoomLevel);
        List<Object[]> results = restaurantRepository.findRestaurantsInAreaForMapView(
                minLat, maxLat, minLng, maxLng, minRating);

        return results.stream().map(row -> {
            RestaurantMapViewDto dto = new RestaurantMapViewDto();
            dto.setRestaurantId(UUID.fromString(row[0].toString()));
            dto.setName((String) row[1]);
            dto.setAddress((String) row[2]);
            dto.setWardId(((Number) row[3]).intValue());
            dto.setLatitude((BigDecimal) row[4]);
            dto.setLongitude((BigDecimal) row[5]);
            dto.setAverageRating((BigDecimal) row[6]);
            dto.setTotalReviews(((Number) row[7]).intValue());
            dto.setPhoto((String) row[8]);
            return dto;
        }).toList();
    }
}
