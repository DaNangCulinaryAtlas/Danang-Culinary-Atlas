//package com.atlasculinary.mappers;
//
//import com.atlasculinary.dtos.AddRestaurantRequest;
//import com.atlasculinary.dtos.RestaurantDto;
//import com.atlasculinary.dtos.RestaurantMapViewDto;
//import com.atlasculinary.dtos.RestaurantTagDto;
//import com.atlasculinary.dtos.UpdateRestaurantRequest;
//import com.atlasculinary.entities.Restaurant;
//import com.atlasculinary.entities.RestaurantTagMap;
//import lombok.AllArgsConstructor;
//import org.mapstruct.*;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Mapper(componentModel="spring", uses = {RestaurantTagMapper.class})
//public interface RestaurantMapper {
//    @Mapping(source = "ownerAccount.accountId", target = "ownerAccountId")
//    @Mapping(source = "ward.wardId", target = "wardId")
//    @Mapping(source = "approvedByAccount.accountId", target = "approvedByAccountId")
//    @Mapping(source = "restaurantStats.averageRating", target = "averageRating")
//    @Mapping(source = "restaurantStats.totalReviews", target = "totalReviews")
////    @Mapping(target = "tags", expression = "java(entity.getTagDtos())")
//    RestaurantDto toDto(Restaurant entity);
//    @Mapping(target = "photo", expression = "java(extractPhoto(entity))")
//    RestaurantMapViewDto toMapViewDto(Restaurant entity);
//
//    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
//    void updateRestaurantFromRequest(
//            UpdateRestaurantRequest request,
//            @MappingTarget Restaurant targetEntity
//    );
//
//    Restaurant toEntity(AddRestaurantRequest request);
//
//    List<RestaurantDto> toDtoList(List<Restaurant> restaurantList);
//
//    List<RestaurantMapViewDto> toMapViewDtoList(List<Restaurant> restaurantList);
//
//    default String extractPhoto(Restaurant restaurant) {
//        if (restaurant.getImages() != null && restaurant.getImages().containsKey("photo")) {
//            Object photoObj = restaurant.getImages().get("photo");
//            return photoObj != null ? photoObj.toString() : null;
//        }
//        return null;
//    }
//
//}


package com.atlasculinary.mappers;

import com.atlasculinary.dtos.*;
import com.atlasculinary.entities.Restaurant;
import com.atlasculinary.entities.RestaurantTagMap;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {RestaurantTagMapper.class})
public abstract class RestaurantMapper {

    @Autowired
    protected RestaurantTagMapper restaurantTagMapper;

    @Mapping(source = "ownerAccount.accountId", target = "ownerAccountId")
    @Mapping(source = "ward.wardId", target = "wardId")
    @Mapping(source = "approvedByAccount.accountId", target = "approvedByAccountId")
    @Mapping(source = "restaurantStats.averageRating", target = "averageRating")
    @Mapping(source = "restaurantStats.totalReviews", target = "totalReviews")
    @Mapping(source = "restaurantTagMapSet", target = "tags")
    public abstract RestaurantDto toDto(Restaurant entity);

    @Mapping(target = "photo", expression = "java(extractPhoto(entity))")
    public abstract RestaurantMapViewDto toMapViewDto(Restaurant entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateRestaurantFromRequest(
            UpdateRestaurantRequest request,
            @MappingTarget Restaurant targetEntity
    );

    public abstract Restaurant toEntity(AddRestaurantRequest request);

    public abstract List<RestaurantDto> toDtoList(List<Restaurant> restaurantList);

    public abstract List<RestaurantMapViewDto> toMapViewDtoList(List<Restaurant> restaurantList);

    protected List<RestaurantTagDto> mapRestaurantTags(Set<RestaurantTagMap> maps) {
        if (maps == null) {
            return null;
        }
        return maps.stream()
                .map(RestaurantTagMap::getRestaurantTag)
                .map(tag -> restaurantTagMapper.toDto(tag))
                .collect(Collectors.toList());
    }

    protected String extractPhoto(Restaurant restaurant) {
        if (restaurant.getImages() != null && restaurant.getImages().containsKey("photo")) {
            Object photoObj = restaurant.getImages().get("photo");
            return photoObj != null ? photoObj.toString() : null;
        }
        return null;
    }
}