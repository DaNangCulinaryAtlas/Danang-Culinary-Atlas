package com.atlasculinary.controllers;

import com.atlasculinary.dtos.AddReviewRequest;
import com.atlasculinary.dtos.ReviewDto;
import com.atlasculinary.dtos.ReviewReplyRequest;
import com.atlasculinary.dtos.UpdateReviewRequest;
import com.atlasculinary.services.ReviewService;
import com.atlasculinary.securities.CustomAccountDetails;
import com.atlasculinary.services.VendorReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Tag(name = "Review Management", description = "API for managing review data")
public class ReviewController {

    private final ReviewService reviewService;
    private final VendorReviewService vendorReviewService;

    @Operation(summary = "Create a new review")
    @PostMapping("/reviews")
    @PreAuthorize("hasAuthority('REVIEW_CREATE')")
    public ResponseEntity<ReviewDto> addReview(
            @Valid @RequestBody AddReviewRequest request,
            @AuthenticationPrincipal CustomAccountDetails principal) {

        ReviewDto newReview = reviewService.addReview(request, principal.getAccountId());
        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing review")
    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasAuthority('REVIEW_UPDATE')")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            @AuthenticationPrincipal CustomAccountDetails principal) {

        ReviewDto updatedReview = reviewService.updateReview(reviewId, request, principal.getAccountId());
        return ResponseEntity.ok(updatedReview);
    }


    @Operation(summary = "Delete a review by ID")
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasAuthority('REVIEW_DELETE')")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomAccountDetails principal) {

        reviewService.deleteReview(reviewId, principal.getAccountId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a review by its ID")
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable UUID reviewId) {
        ReviewDto review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }


    @Operation(summary = "Get reviews by restaurant ID")
    @GetMapping("/restaurants/{restaurantId}/reviews")
    public ResponseEntity<Page<ReviewDto>> getReviewsByRestaurant(
            @PathVariable UUID restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<ReviewDto> reviews = reviewService.getReviewsByRestaurant(restaurantId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get reviews by dish ID")
    @GetMapping("/dishes/{dishId}/reviews")
    public ResponseEntity<Page<ReviewDto>> getReviewsByDish(
            @PathVariable UUID dishId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Page<ReviewDto> reviews = reviewService.getReviewsByDish(dishId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Vendor replies to a specific customer review")
    @PostMapping("/reviews/{reviewId}/reply") // POST /api/v1/reviews/{reviewId}/reply
    @PreAuthorize("hasAuthority('REVIEW_REPLY')")
    public ResponseEntity<ReviewDto> replyToReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewReplyRequest request,
            @AuthenticationPrincipal CustomAccountDetails principal) {

        ReviewDto updatedReview = vendorReviewService.replyToReview(reviewId, request, principal.getAccountId());
        return ResponseEntity.ok(updatedReview);
    }


    @Operation(summary = "Vendor gets all reviews for their specific restaurants")
    @GetMapping("/restaurants/{restaurantId}/vendor-reviews") // GET /api/v1/restaurants/{restaurantId}/vendor-reviews
    @PreAuthorize("hasAuthority('REVIEW_VIEW_VENDOR')")
    public ResponseEntity<Page<ReviewDto>> getVendorReviews(
            @PathVariable UUID restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Page<ReviewDto> reviews = vendorReviewService.getVendorReviews(
                restaurantId, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(reviews);
    }
}