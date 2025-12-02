package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.*;
import com.atlasculinary.entities.Account;
import com.atlasculinary.entities.Restaurant;
import com.atlasculinary.entities.Review;
import com.atlasculinary.exceptions.ResourceNotFoundException;
import com.atlasculinary.mappers.ReviewMapper;
import com.atlasculinary.repositories.RestaurantRepository;
import com.atlasculinary.repositories.ReviewRepository;
import com.atlasculinary.services.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AccountService accountService;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantStatsService restaurantStatsService;
    private final NotificationService notificationService;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewDto addReview(AddReviewRequest request, UUID userId) {
        Review review = reviewMapper.toEntity(request);
        UUID restaurantId = request.getRestaurantId();
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(()-> new ResourceNotFoundException("Restaurant not found with ID " + restaurantId));

        Account user = accountService.getAccountById(userId);
        review.setRestaurant(restaurant);
        review.setReviewerAccount(user);
        review.setCreatedAt(LocalDateTime.now());
        var reviewSaved = reviewRepository.save(review);

        Integer newRating = reviewSaved.getRating();
        // Notifications
        notificationService.notifyVendorNewUserReview(reviewSaved.getReviewId());
        // RestaurantStats
        restaurantStatsService.updateStatsOnReviewEvent(
                restaurantId,
                null,
                newRating
        );
        return reviewMapper.toDto(reviewSaved);
    }

    @Override
    @Transactional
    public ReviewDto updateReview(UUID reviewId, UpdateReviewRequest request, UUID userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        Integer oldRating = review.getRating();
        UUID restaurantId = review.getRestaurant().getRestaurantId();
        UUID ownerId = review.getReviewerAccount().getAccountId();
        boolean isAdmin = accountService.isAdmin(userId);

        if (!ownerId.equals(userId) && !isAdmin) {
            throw new SecurityException("Bạn không có quyền sửa bình luận này.");
        }

        reviewMapper.updateEntityFromRequest(request, review);
        var reviewUpdated = reviewRepository.save(review);

        Integer newRating = reviewUpdated.getRating();
        // RestaurantStats
        restaurantStatsService.updateStatsOnReviewEvent(
                restaurantId,
                oldRating,
                newRating
        );
        return reviewMapper.toDto(reviewUpdated);
    }

    @Override
    @Transactional
    public void deleteReview(UUID reviewId, UUID userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        UUID restaurantId = review.getRestaurant().getRestaurantId();
        Integer oldRating = review.getRating();
        UUID ownerId = review.getReviewerAccount().getAccountId();
        boolean isAdmin = accountService.isAdmin(userId);

        if (!ownerId.equals(userId) && !isAdmin) {
            throw new SecurityException("Bạn không có quyền sửa bình luận này.");
        }
        // RestaurantStats
        restaurantStatsService.updateStatsOnReviewEvent(
                restaurantId,
                oldRating,
                null
        );
        reviewRepository.delete(review);
    }

    @Override
    public Page<ReviewDto> getReviewsByRestaurant(UUID restId, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviewPage = reviewRepository.findByRestaurant_RestaurantId(restId, pageable);

        return reviewPage.map(reviewMapper::toDto);

    }

    @Override
    public Page<ReviewDto> getReviewsByDish(UUID dishId, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviewPage = reviewRepository.findByDish_DishId(dishId, pageable);

        return reviewPage.map(reviewMapper::toDto);
    }

    @Override
    public ReviewDto getReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        return reviewMapper.toDto(review);
    }

    @Override
    public Page<ReviewDto> getReviewsByRatingRange(Integer minRating, Integer maxRating, int page, int size, String sortBy, String sortDirection) {
        validateRatingRange(minRating, maxRating);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviewPage = reviewRepository.findByRatingBetween(minRating, maxRating, pageable);
        return reviewPage.map(reviewMapper::toDto);
    }

    @Override
    public Page<ReviewDto> getReviewsByRestaurantAndRatingRange(UUID restId, Integer minRating, Integer maxRating, int page, int size, String sortBy, String sortDirection) {
        validateRatingRange(minRating, maxRating);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviewPage = reviewRepository.findByRestaurant_RestaurantIdAndRatingBetween(restId, minRating, maxRating, pageable);
        return reviewPage.map(reviewMapper::toDto);
    }

    @Override
    public Page<ReviewDto> getReviewsByDishAndRatingRange(UUID dishId, Integer minRating, Integer maxRating, int page, int size, String sortBy, String sortDirection) {
        validateRatingRange(minRating, maxRating);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviewPage = reviewRepository.findByDish_DishIdAndRatingBetween(dishId, minRating, maxRating, pageable);
        return reviewPage.map(reviewMapper::toDto);
    }

    private void validateRatingRange(Integer minRating, Integer maxRating) {
        if (minRating < 1 || minRating > 5) {
            throw new IllegalArgumentException("minRating must be between 1 and 5");
        }
        if (maxRating < 1 || maxRating > 5) {
            throw new IllegalArgumentException("maxRating must be between 1 and 5");
        }
        if (minRating > maxRating) {
            throw new IllegalArgumentException("minRating cannot be greater than maxRating");
        }
    }
}
