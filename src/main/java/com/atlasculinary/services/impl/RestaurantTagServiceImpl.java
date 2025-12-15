package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.RestaurantTagDto;
import com.atlasculinary.entities.Restaurant;
import com.atlasculinary.entities.RestaurantTag;
import com.atlasculinary.entities.RestaurantTagMap;
import com.atlasculinary.mappers.RestaurantTagMapper;
import com.atlasculinary.repositories.RestaurantRepository;
import com.atlasculinary.repositories.RestaurantTagMapRepository;
import com.atlasculinary.repositories.RestaurantTagRepository;
import com.atlasculinary.services.RestaurantTagService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RestaurantTagServiceImpl implements RestaurantTagService {
    private final RestaurantTagRepository restaurantTagRepository;
    private final RestaurantTagMapper restaurantTagMapper;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTagMapRepository restaurantTagMapRepository;
    @Override
    public List<RestaurantTagDto> getAllRestaurantTag() {
        List<RestaurantTag> restaurantTagList = restaurantTagRepository.findAll();
        return restaurantTagMapper.toDtoList(restaurantTagList);
    }

    @Override
    public void addTagsToRestaurant(UUID restaurantId, List<Long> tagIds) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + restaurantId));

        List<RestaurantTagMap> mappings = tagIds.stream()
                .map(tagId -> {
                    RestaurantTag tag = restaurantTagRepository.findById(tagId)
                            .orElseThrow(() -> new RuntimeException("Tag not found with ID: " + tagId));

                    RestaurantTagMap map = new RestaurantTagMap();
                    map.setRestaurantId(restaurantId);
                    map.setRestaurant(restaurant);
                    map.setTagId(tagId);
                    map.setRestaurantTag(tag);

                    return map;
                })
                .collect(Collectors.toList());

        restaurantTagMapRepository.saveAll(mappings);
    }

    @Override
    @Transactional
    public void updateTagsForRestaurant(UUID restaurantId, List<Long> tagIds) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + restaurantId));


        restaurantTagMapRepository.deleteByRestaurantId(restaurantId);
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<RestaurantTag> existingTags = restaurantTagRepository.findAllById(tagIds);

        if (existingTags.size() != tagIds.size()) {
            List<Long> existingIds = existingTags.stream()
                    .map(RestaurantTag::getTagId)
                    .toList();

            tagIds.removeAll(existingIds);
            throw new RuntimeException("One or more Tags not found with IDs: " + tagIds);
        }

        List<RestaurantTagMap> newMappings = existingTags.stream()
                .map(tag -> {
                    RestaurantTagMap map = new RestaurantTagMap();
                    map.setRestaurant(restaurant);
                    map.setRestaurantTag(tag);
                    map.setRestaurantId(restaurantId);
                    map.setTagId(tag.getTagId());
                    return map;
                })
                .collect(Collectors.toList());
        restaurantTagMapRepository.saveAll(newMappings);
    }

    @Override
    public List<RestaurantTagDto> getRestaurantTagsByRestaurantId(UUID restaurantId) {

        List<RestaurantTagMap> tagMaps =
                restaurantTagMapRepository.findByRestaurantId(restaurantId);

        if (tagMaps.isEmpty()) {
            return List.of();
        }

        List<RestaurantTag> tags = tagMaps.stream()
                .map(RestaurantTagMap::getRestaurantTag)
                .collect(Collectors.toList());

        return restaurantTagMapper.toDtoList(tags);
    }

    @Override
    public void deleteRestaurantTagsByRestaurantId(UUID restaurantId) {
        restaurantTagMapRepository.deleteByRestaurantId(restaurantId);

    }
}
