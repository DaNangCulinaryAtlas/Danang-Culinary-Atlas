package com.atlasculinary.services;

import com.atlasculinary.dtos.AddReviewRequest;
import com.atlasculinary.dtos.ReviewDto;
import com.atlasculinary.dtos.UpdateReviewRequest;
import org.springframework.data.domain.Page;
import java.util.UUID;

public interface ReviewService {
    ReviewDto addReview(AddReviewRequest request, UUID userId);

    ReviewDto updateReview(UUID reviewId, UpdateReviewRequest request, UUID userId);

    void deleteReview(UUID reviewId, UUID userId);

    Page<ReviewDto> getReviewsByRestaurant(UUID restId, int page, int size, String sortBy, String sortDirection);

    Page<ReviewDto> getReviewsByDish(UUID dishId, int page, int size, String sortBy, String sortDirection);

    ReviewDto getReviewById(UUID reviewId);
    
    Page<ReviewDto> getReviewsByRatingRange(Integer minRating, Integer maxRating, int page, int size, String sortBy, String sortDirection);
    
    Page<ReviewDto> getReviewsByRestaurantAndRatingRange(UUID restId, Integer minRating, Integer maxRating, int page, int size, String sortBy, String sortDirection);
}
