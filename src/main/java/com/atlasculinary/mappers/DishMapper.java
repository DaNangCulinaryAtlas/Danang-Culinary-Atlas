package com.atlasculinary.mappers;

import com.atlasculinary.dtos.AddDishRequest;
import com.atlasculinary.dtos.DishDto;
import com.atlasculinary.dtos.UpdateDishRequest;
import com.atlasculinary.entities.Dish;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DishMapper {
    @Mapping(target = "restaurantId", expression = "java(dish.getRestaurant() != null ? dish.getRestaurant().getRestaurantId() : null)")
    DishDto toDto(Dish dish);

    List<DishDto> toDtoList(List<Dish> dishList);

    Dish toEntity(AddDishRequest addDishRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDishFromRequest(
            UpdateDishRequest request,
            @MappingTarget Dish targetEntity
    );

}
