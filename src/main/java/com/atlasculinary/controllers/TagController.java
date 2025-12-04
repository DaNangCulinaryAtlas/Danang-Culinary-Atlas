package com.atlasculinary.controllers;

import com.atlasculinary.dtos.DishTagDto;
import com.atlasculinary.dtos.RestaurantTagDto;
import com.atlasculinary.services.DishTagService;
import com.atlasculinary.services.RestaurantTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
@AllArgsConstructor
@Tag(name = "Tag Management", description = "API for managing restaurant and dish tags")
public class TagController {

    private final RestaurantTagService restaurantTagService;
    private final DishTagService dishTagService;
    @Operation(summary = "Get all restaurant tags", description = "Retrieve all available tags that can be applied to restaurants")
    @GetMapping("/restaurant")
    @PreAuthorize("hasAuthority('TAG_VIEW_RESTAURANT')")
    public ResponseEntity<List<RestaurantTagDto>> getAllRestaurantTags() {
        List<RestaurantTagDto> tags = restaurantTagService.getAllRestaurantTag();
        return ResponseEntity.ok(tags);
    }

    @Operation(summary = "Get all dish tags", description = "Retrieve all available tags that can be applied to dishes")
    @GetMapping("/dish")
    @PreAuthorize("hasAuthority('TAG_VIEW_DISH')")
    public ResponseEntity<List<DishTagDto>> getAllDishTags() {
        List<DishTagDto> tags = dishTagService.getAllDishTag();
        return ResponseEntity.ok(tags);
    }

    @Operation(summary = "Get tags for a specific restaurant", description = "Retrieve all tags associated with the given restaurant ID")
    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAuthority('TAG_VIEW_BY_RESTAURANT')")
    public ResponseEntity<List<RestaurantTagDto>> getRestaurantTagsByRestaurantId(@PathVariable UUID restaurantId) {

        List<RestaurantTagDto> tags = restaurantTagService.getRestaurantTagsByRestaurantId(restaurantId);
        if (tags.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(tags);
    }
}