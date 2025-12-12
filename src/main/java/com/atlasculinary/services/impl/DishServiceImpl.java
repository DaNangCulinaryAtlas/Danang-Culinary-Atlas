package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.AddDishRequest;
import com.atlasculinary.dtos.DishDto;
import com.atlasculinary.dtos.UpdateDishRequest;
import com.atlasculinary.dtos.UpdateDishStatusRequest;
import com.atlasculinary.entities.Dish;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.DishStatus;
import com.atlasculinary.exceptions.ResourceNotFoundException;
import com.atlasculinary.mappers.DishMapper;
import com.atlasculinary.repositories.DishRepository;
import com.atlasculinary.repositories.RestaurantRepository;
import com.atlasculinary.securities.CustomAccountDetails;
import com.atlasculinary.services.AccountService;
import com.atlasculinary.services.DishService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class DishServiceImpl implements DishService {
    private static final Logger LOGGER = Logger.getLogger(DishServiceImpl.class.getName());
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;
    private final RestaurantRepository restaurantRepository;
    private final AccountService accountService;
    
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public DishDto createDish(AddDishRequest request, UUID accessAccountId) {
        UUID restaurantId = request.getRestaurantId();
        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(()-> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        UUID ownerId = restaurant.getOwnerAccount().getAccountId();
        boolean isAdmin = accountService.isAdmin(accessAccountId);

        if (!ownerId.equals(accessAccountId) && !isAdmin) {
            throw new SecurityException("Bạn không có quyền thêm món ăn.");
        }
        Dish dish = dishMapper.toEntity(request);
        dish.setRestaurant(restaurant);
        dish.setApprovalStatus(ApprovalStatus.PENDING);

        var dishSaved = dishRepository.save(dish);

        return dishMapper.toDto(dishSaved);

    }

    @Override
    public DishDto updateDish(UUID dishId, UpdateDishRequest request, UUID accessAccountId) {

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(()-> new ResourceNotFoundException("Dish not found with ID: " + dishId));

        UUID ownerId = dish.getRestaurant().getOwnerAccount().getAccountId();
        boolean isAdmin = accountService.isAdmin(accessAccountId);

        if (!ownerId.equals(accessAccountId) && !isAdmin) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa thông tin món ăn này.");
        }



        dishMapper.updateDishFromRequest(request, dish);

        dishRepository.save(dish);
        return dishMapper.toDto(dish);

    }

    @Override
    public DishDto updateDishStatus(UUID dishId, UpdateDishStatusRequest request, UUID accessAccountId) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(()-> new ResourceNotFoundException("Dish not found with ID: " + dishId));

        UUID ownerId = dish.getRestaurant().getOwnerAccount().getAccountId();
        boolean isAdmin = accountService.isAdmin(accessAccountId);

        if (!ownerId.equals(accessAccountId) && !isAdmin) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa thông tin món ăn này.");
        }

        dish.setStatus(request.getStatus());

        dishRepository.save(dish);
        return dishMapper.toDto(dish);
    }

    @Override
    public DishDto getDishById(UUID dishId) {
        // Tìm món ăn
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn không tìm thấy với ID: " + dishId));

        // Kiểm tra điều kiện công khai
        if (dish.getApprovalStatus() != ApprovalStatus.APPROVED ||
                dish.getStatus() != DishStatus.AVAILABLE) {

            // Dùng ResourceNotFoundException để ẩn sự tồn tại của món ăn nếu không công khai
            throw new ResourceNotFoundException("Món ăn không tìm thấy hoặc không khả dụng.");
        }

        return dishMapper.toDto(dish);
    }

    @Override
    public DishDto getDishDetailsForManagement(UUID dishId, CustomAccountDetails principal) {

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn không tìm thấy với ID: " + dishId));

        UUID accountId = principal.getAccountId();
        Collection<? extends GrantedAuthority> roles = principal.getAuthorities();

        boolean hasAdminRole = roles.stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        boolean hasVendorRole = roles.stream().anyMatch(a -> a.getAuthority().equals("VENDOR"));

        if (hasAdminRole) {
            return dishMapper.toDto(dish);
        }

        if (hasVendorRole) {
            UUID ownerAccountId = dish.getRestaurant().getOwnerAccount().getAccountId();

            if (ownerAccountId.equals(accountId)) {
                return dishMapper.toDto(dish);
            }
        }
        throw new AccessDeniedException("Bạn không có quyền truy cập thông tin chi tiết món ăn này.");
    }


    @Override
    public Page<DishDto> getRestaurantDishes(UUID restaurantId, int page, int size, String sortBy, String sortDirection, UUID accessAccountId) {

        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(()-> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        UUID ownerId = restaurant.getOwnerAccount().getAccountId();
        boolean isAdmin = accountService.isAdmin(accessAccountId);

        if (!ownerId.equals(accessAccountId) && !isAdmin) {
            throw new SecurityException("Bạn không có quyền lấy danh sách món ăn.");
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC :
                Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Dish> dishPage = dishRepository.findByRestaurant_RestaurantId(restaurantId, pageable);

        return dishPage.map(dishMapper::toDto);
    }

    @Override
    public Page<DishDto> getAvailableDishes(UUID restaurantId, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC :
                Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Dish> dishPage = dishRepository.findByRestaurant_RestaurantIdAndStatusAndApprovalStatus(restaurantId,
                DishStatus.AVAILABLE,
                ApprovalStatus.APPROVED,
                pageable);

        return dishPage.map(dishMapper::toDto);
    }

    @Override
    public Page<DishDto> searchDishes(
            List<Long> tagIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            String status,
            int page,
            int size,
            String sortBy,
            String sortOrder) {

        // Validate sortBy field để tránh SQL injection
        String validSortBy = validateSortBy(sortBy);
        
        // Build query động để tránh lỗi PostgreSQL với NULL parameters
        StringBuilder queryBuilder = new StringBuilder(
            "SELECT DISTINCT d.dish_id FROM dish d " +
            "LEFT JOIN dish_tag_map dtm ON dtm.dish_id = d.dish_id " +
            "LEFT JOIN dish_tag dt ON dt.tag_id = dtm.tag_id " +
            "WHERE d.approval_status = :approvalStatus"
        );
        
        boolean hasTagFilter = tagIds != null && !tagIds.isEmpty();
        boolean hasMinPrice = minPrice != null;
        boolean hasMaxPrice = maxPrice != null;
        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasStatusFilter = status != null && !status.trim().isEmpty();
        
        if (hasStatusFilter) {
            queryBuilder.append(" AND d.status = :status");
        }
        if (hasTagFilter) {
            queryBuilder.append(" AND dt.tag_id IN (:tagIds)");
        }
        if (hasMinPrice) {
            queryBuilder.append(" AND d.price >= :minPrice");
        }
        if (hasMaxPrice) {
            queryBuilder.append(" AND d.price <= :maxPrice");
        }
        if (hasSearch) {
            queryBuilder.append(" AND LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))");
        }
        
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        query.setParameter("approvalStatus", ApprovalStatus.APPROVED.name());
        if (hasStatusFilter) {
            try {
                DishStatus dishStatus = DishStatus.valueOf(status.trim().toUpperCase());
                query.setParameter("status", dishStatus.name());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status value. Must be one of: AVAILABLE, SOLD_OUT, HIDDEN");
            }
        }
        if (hasTagFilter) {
            query.setParameter("tagIds", tagIds);
        }
        if (hasMinPrice) {
            query.setParameter("minPrice", minPrice);
        }
        if (hasMaxPrice) {
            query.setParameter("maxPrice", maxPrice);
        }
        if (hasSearch) {
            query.setParameter("search", search.trim());
        }
        
        // Execute query để lấy dish_ids
        @SuppressWarnings("unchecked")
        List<Object> results = query.getResultList();
        List<UUID> dishIds = results.stream()
                .map(obj -> UUID.fromString(obj.toString()))
                .collect(Collectors.toList());

        // Build count query tương tự
        StringBuilder countQueryBuilder = new StringBuilder(
            "SELECT COUNT(DISTINCT d.dish_id) FROM dish d " +
            "LEFT JOIN dish_tag_map dtm ON dtm.dish_id = d.dish_id " +
            "LEFT JOIN dish_tag dt ON dt.tag_id = dtm.tag_id " +
            "WHERE d.approval_status = :approvalStatus"
        );
        
        if (hasStatusFilter) {
            countQueryBuilder.append(" AND d.status = :status");
        }
        if (hasTagFilter) {
            countQueryBuilder.append(" AND dt.tag_id IN (:tagIds)");
        }
        if (hasMinPrice) {
            countQueryBuilder.append(" AND d.price >= :minPrice");
        }
        if (hasMaxPrice) {
            countQueryBuilder.append(" AND d.price <= :maxPrice");
        }
        if (hasSearch) {
            countQueryBuilder.append(" AND LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))");
        }
        
        Query countQuery = entityManager.createNativeQuery(countQueryBuilder.toString());
        countQuery.setParameter("approvalStatus", ApprovalStatus.APPROVED.name());
        if (hasStatusFilter) {
            try {
                DishStatus dishStatus = DishStatus.valueOf(status.trim().toUpperCase());
                countQuery.setParameter("status", dishStatus.name());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status value. Must be one of: AVAILABLE, SOLD_OUT, HIDDEN");
            }
        }
        if (hasTagFilter) {
            countQuery.setParameter("tagIds", tagIds);
        }
        if (hasMinPrice) {
            countQuery.setParameter("minPrice", minPrice);
        }
        if (hasMaxPrice) {
            countQuery.setParameter("maxPrice", maxPrice);
        }
        if (hasSearch) {
            countQuery.setParameter("search", search.trim());
        }
        
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Sort.Direction direction = sortOrder != null && sortOrder.equalsIgnoreCase("desc") ?
            Sort.Direction.DESC :
            Sort.Direction.ASC;
        Sort sort = Sort.by(direction, validSortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        if (dishIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // Query dishes theo dishIds với sort và pagination
        List<Dish> allDishes = new ArrayList<>(dishRepository.findAllById(dishIds));
        
        // Sort thủ công theo sortBy và sortOrder
        allDishes.sort((d1, d2) -> {
            int comparison = 0;
            switch (validSortBy) {
                case "name":
                    comparison = d1.getName().compareToIgnoreCase(d2.getName());
                    break;
                case "price":
                    comparison = d1.getPrice().compareTo(d2.getPrice());
                    break;
                case "createdat":
                    // Dish không có createdAt field, dùng dishId làm fallback
                    comparison = d1.getDishId().compareTo(d2.getDishId());
                    break;
                case "dishid":
                    comparison = d1.getDishId().compareTo(d2.getDishId());
                    break;
                default:
                    comparison = d1.getName().compareToIgnoreCase(d2.getName());
            }
            return direction == Sort.Direction.DESC ? -comparison : comparison;
        });

        // Paginate thủ công
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allDishes.size());
        List<Dish> pagedDishes = start < allDishes.size() ? new ArrayList<>(allDishes.subList(start, end)) : new ArrayList<>();

        // Map to DTOs safely using mapper (handles array cloning) and set restaurantId separately
        List<DishDto> dtoList = new ArrayList<>(pagedDishes.size());
        for (Dish dish : pagedDishes) {
            DishDto dto = dishMapper.toDto(dish);
            // Set restaurantId cautiously (avoid initializing lazy proxies)
            try {
                if (dish.getRestaurant() != null) {
                    dto.setRestaurantId(dish.getRestaurant().getRestaurantId());
                }
            } catch (RuntimeException ignored) {
                dto.setRestaurantId(null);
            }
            dtoList.add(dto);
        }

        Page<DishDto> dtoPage = new org.springframework.data.domain.PageImpl<>(dtoList, pageable, total);
        return dtoPage;
    }

    private String validateSortBy(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "name"; // Default sort
        }
        
        // Whitelist các field hợp lệ để tránh SQL injection
        List<String> allowedFields = List.of("name", "price", "createdAt", "dishId");
        String normalizedSortBy = sortBy.trim().toLowerCase();
        
        if (allowedFields.contains(normalizedSortBy)) {
            return normalizedSortBy;
        }
        
        return "name"; // Fallback to default
    }
}
