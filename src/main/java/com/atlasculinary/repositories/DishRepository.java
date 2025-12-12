package com.atlasculinary.repositories;

import com.atlasculinary.entities.Dish;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.DishStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<Dish, UUID> {
    Page<Dish> findByRestaurant_RestaurantId(UUID restaurantId, Pageable pageable);

    Page<Dish> findByRestaurant_RestaurantIdAndStatusAndApprovalStatus(
            UUID restaurantId,
            DishStatus status,
            ApprovalStatus approvalStatus,
            Pageable pageable
    );

    Page<Dish> findByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);

    Long countByRestaurant_OwnerAccount_AccountId(UUID vendorAccountId);

    @Query(value = "SELECT DISTINCT d.dish_id FROM dish d " +
           "LEFT JOIN dish_tag_map dtm ON dtm.dish_id = d.dish_id " +
           "LEFT JOIN dish_tag dt ON dt.tag_id = dtm.tag_id " +
           "WHERE d.status = :status " +
           "AND d.approval_status = :approvalStatus " +
           "AND (:hasTagFilter = false OR dt.name IN (:tagNames)) " +
           "AND (:hasMinPrice = false OR d.price >= :minPrice) " +
           "AND (:hasMaxPrice = false OR d.price <= :maxPrice) " +
           "AND (:hasSearch = false OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    List<UUID> findDishIdsByFilters(
            @Param("status") String status,
            @Param("approvalStatus") String approvalStatus,
            @Param("hasTagFilter") boolean hasTagFilter,
            @Param("tagNames") List<String> tagNames,
            @Param("hasMinPrice") boolean hasMinPrice,
            @Param("minPrice") BigDecimal minPrice,
            @Param("hasMaxPrice") boolean hasMaxPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("hasSearch") boolean hasSearch,
            @Param("search") String search
    );

    @Query(value = "SELECT COUNT(DISTINCT d.dish_id) FROM dish d " +
           "LEFT JOIN dish_tag_map dtm ON dtm.dish_id = d.dish_id " +
           "LEFT JOIN dish_tag dt ON dt.tag_id = dtm.tag_id " +
           "WHERE d.status = :status " +
           "AND d.approval_status = :approvalStatus " +
           "AND (:hasTagFilter = false OR dt.name IN (:tagNames)) " +
           "AND (:hasMinPrice = false OR d.price >= :minPrice) " +
           "AND (:hasMaxPrice = false OR d.price <= :maxPrice) " +
           "AND (:hasSearch = false OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    long countDishesByFilters(
            @Param("status") String status,
            @Param("approvalStatus") String approvalStatus,
            @Param("hasTagFilter") boolean hasTagFilter,
            @Param("tagNames") List<String> tagNames,
            @Param("hasMinPrice") boolean hasMinPrice,
            @Param("minPrice") BigDecimal minPrice,
            @Param("hasMaxPrice") boolean hasMaxPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("hasSearch") boolean hasSearch,
            @Param("search") String search
    );
}
