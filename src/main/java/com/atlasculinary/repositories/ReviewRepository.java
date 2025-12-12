package com.atlasculinary.repositories;

import com.atlasculinary.entities.Account;
import com.atlasculinary.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByRestaurant_RestaurantId(UUID restId, Pageable pageable);

    Page<Review> findByDish_DishId(UUID dishId, Pageable pageable);
    
    Long countByReviewerAccount(Account reviewerAccount);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.restaurant.ownerAccount.accountId = :vendorAccountId")
    Long countByVendorAccountId(@Param("vendorAccountId") UUID vendorAccountId);
    
    Page<Review> findByRatingBetween(Integer minRating, Integer maxRating, Pageable pageable);
    
    Page<Review> findByRestaurant_RestaurantIdAndRatingBetween(UUID restId, Integer minRating, Integer maxRating, Pageable pageable);
}
