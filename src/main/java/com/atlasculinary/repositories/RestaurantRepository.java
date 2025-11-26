package com.atlasculinary.repositories;

import com.atlasculinary.entities.Restaurant;
import com.atlasculinary.enums.ApprovalStatus;
import com.atlasculinary.enums.RestaurantStatus;
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
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    Page<Restaurant> findByOwnerAccount_AccountId(UUID vendorId, Pageable pageable);

    Page<Restaurant> findAllByStatusAndApprovalStatus(Pageable pageable, RestaurantStatus restaurantStatus, ApprovalStatus approvalStatus);

    @Query(value = "SELECT r.restaurant_id as restaurantId, r.name as name, r.address as address, " +
            "r.ward_id as wardId, r.latitude as latitude, r.longitude as longitude, " +
            "rs.average_rating as averageRating, rs.total_reviews as totalReviews, " +
            "r.images->>'photo' as photo " +
            "FROM restaurant r " +
            "JOIN restaurant_stats rs ON r.restaurant_id = rs.restaurant_id " +
            "WHERE r.latitude BETWEEN :minLat AND :maxLat " +
            "AND r.longitude BETWEEN :minLng AND :maxLng " +
            "AND rs.average_rating >= :minRating " +
            "AND r.approval_status = 'APPROVED'",
            nativeQuery = true)
    List<Object[]> findRestaurantsInAreaForMapView(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng,
            @Param("minRating") BigDecimal minRating);

    @Query(value = "SELECT r.* " +
            "FROM restaurant r " +
            "JOIN restaurant_stats rs ON r.restaurant_id = rs.restaurant_id " +
            "WHERE r.approval_status = 'APPROVED' " +
            "AND rs.average_rating BETWEEN :minRating AND :maxRating " +

            "AND EXISTS (" +
            "SELECT 1 FROM restaurant_tag_map rtm " +
            "JOIN restaurant_tag rt ON rtm.tag_id = rt.tag_id " +
            "WHERE rtm.restaurant_id = r.restaurant_id " +
            "AND rt.name IN (:cuisineTypes)" +
            ")",

            countQuery = "SELECT count(r.restaurant_id) " +
                    "FROM restaurant r " +
                    "JOIN restaurant_stats rs ON r.restaurant_id = rs.restaurant_id " +
                    "WHERE r.approval_status = 'APPROVED' " +
                    "AND rs.average_rating BETWEEN :minRating AND :maxRating " +
                    "AND (:cuisineTypes IS NULL OR :cuisineTypes = '') OR EXISTS (" +
                    "SELECT 1 FROM restaurant_tag_map rtm " +
                    "JOIN restaurant_tag rt ON rtm.tag_id = rt.tag_id " +
                    "WHERE rtm.restaurant_id = r.restaurant_id " +
                    "AND rt.name IN (:cuisineTypes)" +
                    ")",
            nativeQuery = true)
    Page<Restaurant> findApprovedRestaurantsByCriteria(
            @Param("cuisineTypes") List<String> cuisineTypes,
            @Param("minRating") BigDecimal minRating,
            @Param("maxRating") BigDecimal maxRating,
            Pageable pageable);

    @Query(value = "SELECT r.* " +
            "FROM restaurant r " +
            "JOIN restaurant_stats rs ON r.restaurant_id = rs.restaurant_id " +
            "WHERE r.approval_status = 'APPROVED' " +
            "AND rs.average_rating BETWEEN :minRating AND :maxRating",
            countQuery = "SELECT count(r.restaurant_id) " +
                    "FROM restaurant r " +
                    "JOIN restaurant_stats rs ON r.restaurant_id = rs.restaurant_id " +
                    "WHERE r.approval_status = 'APPROVED' " +
                    "AND rs.average_rating BETWEEN :minRating AND :maxRating " +
                    "AND (:cuisineTypes IS NULL OR :cuisineTypes = '') OR EXISTS (" +
                    "SELECT 1 FROM restaurant_tag_map rtm " +
                    "JOIN restaurant_tag rt ON rtm.tag_id = rt.tag_id " +
                    "WHERE rtm.restaurant_id = r.restaurant_id " +
                    "AND rt.name IN (:cuisineTypes)" +
                    ")",
            nativeQuery = true)
    Page<Restaurant> findApprovedRestaurantsWithoutTag(
            @Param("minRating") BigDecimal minRating,
            @Param("maxRating") BigDecimal maxRating,
            Pageable pageable);

    Page<Restaurant> findByApprovalStatusAndNameContainingIgnoreCase(ApprovalStatus approvalStatus, String restaurantName, Pageable pageable);
    
    Long countByApprovalStatus(ApprovalStatus approvalStatus);
    
    Long countByOwnerAccount(com.atlasculinary.entities.Account ownerAccount);
    
    @Query("SELECT new com.atlasculinary.dtos.RestaurantCountByTagDto(rt.tagId, rt.name, COUNT(r)) " +
           "FROM Restaurant r " +
           "JOIN r.restaurantTagMapSet rtm " +
           "JOIN rtm.restaurantTag rt " +
           "WHERE r.approvalStatus = :approvalStatus " +
           "GROUP BY rt.tagId, rt.name " +
           "ORDER BY COUNT(r) DESC")
    List<com.atlasculinary.dtos.RestaurantCountByTagDto> countRestaurantsByTag(@Param("approvalStatus") ApprovalStatus approvalStatus);
}
