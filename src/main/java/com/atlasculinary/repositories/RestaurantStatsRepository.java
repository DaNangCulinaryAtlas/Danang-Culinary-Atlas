package com.atlasculinary.repositories;

import com.atlasculinary.entities.RestaurantStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RestaurantStatsRepository extends JpaRepository<RestaurantStats, UUID> {

    @Modifying
    @Query("UPDATE RestaurantStats s SET s.totalViews = s.totalViews + 1 WHERE s.restaurantId = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE RestaurantStats s SET s.totalSearches = s.totalSearches + 1 WHERE s.restaurantId = :id")
    void incrementSearchCount(@Param("id") UUID id);
}