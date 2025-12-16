package com.atlasculinary.services;

import com.atlasculinary.entities.Restaurant;

import java.util.List;
import java.util.UUID;

public interface RestaurantStatsService {

    // Chạy khi tạo nhà hàng mới
    void createInitialStats(Restaurant restaurant);

    // --- NHÓM TRAFFIC (Chạy ngầm hoàn toàn - Fire & Forget) ---
    void incrementViewCount(UUID restaurantId);
    void incrementSearchCount(UUID restaurantId);

    // --- NHÓM REVIEW (Được gọi từ ReviewService) ---
    // Chỉ cập nhật số liệu, không trả về gì cả
    void handleNewReview(UUID restaurantId, Integer rating);

    void handleUpdatedReview(UUID restaurantId, Integer oldRating, Integer newRating);

    void handleDeleteReview(UUID restaurantId, Integer rating);

    void incrementSearchCountBatch(List<UUID> foundIds);
}