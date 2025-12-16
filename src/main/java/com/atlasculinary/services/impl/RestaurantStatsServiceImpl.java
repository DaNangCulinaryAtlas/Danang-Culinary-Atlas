package com.atlasculinary.services.impl;

import com.atlasculinary.entities.Restaurant;
import com.atlasculinary.entities.RestaurantStats;
import com.atlasculinary.exceptions.ResourceNotFoundException;
import com.atlasculinary.repositories.RestaurantStatsRepository;
import com.atlasculinary.services.RestaurantStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantStatsServiceImpl implements RestaurantStatsService {

    private final RestaurantStatsRepository statRepository;

    private static final int CALCULATE_SCALE = 5;
    private static final int DISPLAY_SCALE = 1;

    @Override
    @Transactional
    public void createInitialStats(Restaurant restaurant) {
        RestaurantStats stats = new RestaurantStats();
        stats.setRestaurant(restaurant);
        stats.setCreatedAt(LocalDateTime.now());
        stats.setUpdatedAt(LocalDateTime.now());
        statRepository.save(stats);
    }

    // --- TRAFFIC (Async để không làm chậm API chính) ---

    @Override
    @Async
    @Transactional
    public void incrementViewCount(UUID restaurantId) {
        statRepository.incrementViewCount(restaurantId);
    }

    @Override
    @Async
    @Transactional
    public void incrementSearchCount(UUID restaurantId) {
        statRepository.incrementSearchCount(restaurantId);
    }

    // --- REVIEW LOGIC ---

    @Override
    @Transactional
    public void handleNewReview(UUID restaurantId, Integer rating) {
        RestaurantStats stats = getEntityById(restaurantId);
        stats.setTotalReviews(stats.getTotalReviews() + 1);
        stats.setSumOfRatings(stats.getSumOfRatings() + rating);
        recalculateAndSave(stats);
    }

    @Override
    @Transactional
    public void handleUpdatedReview(UUID restaurantId, Integer oldRating, Integer newRating) {
        if (oldRating.equals(newRating)) return;

        RestaurantStats stats = getEntityById(restaurantId);
        stats.setSumOfRatings(stats.getSumOfRatings() - oldRating + newRating);
        recalculateAndSave(stats);
    }

    @Override
    @Transactional
    public void handleDeleteReview(UUID restaurantId, Integer rating) {
        RestaurantStats stats = getEntityById(restaurantId);
        if (stats.getTotalReviews() > 0) {
            stats.setTotalReviews(stats.getTotalReviews() - 1);
            stats.setSumOfRatings(stats.getSumOfRatings() - rating);
        }
        recalculateAndSave(stats);
    }

    @Override
    public void incrementSearchCountBatch(List<UUID> foundIds) {
        for (var Id: foundIds) {
            incrementSearchCount(Id);
        }
    }

    // --- PRIVATE HELPERS ---

    private RestaurantStats getEntityById(UUID id) {
        return statRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantStat not found for ID: " + id));
    }

    private void recalculateAndSave(RestaurantStats stats) {
        int total = stats.getTotalReviews();
        int sum = stats.getSumOfRatings();

        if (total <= 0) {
            stats.setTotalReviews(0);
            stats.setSumOfRatings(0);
            stats.setAverageRating(BigDecimal.ZERO);
        } else {
            BigDecimal sumDec = new BigDecimal(sum);
            BigDecimal totalDec = new BigDecimal(total);

            BigDecimal avg = sumDec.divide(totalDec, CALCULATE_SCALE, RoundingMode.HALF_UP)
                    .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP);

            stats.setAverageRating(avg);
        }

        stats.setUpdatedAt(LocalDateTime.now());
        statRepository.save(stats);
    }
}