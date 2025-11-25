package com.atlasculinary.mappers;

import com.atlasculinary.dtos.ActionDto;
import com.atlasculinary.entities.Action;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActionMapper {
    ActionDto toDto(Action action);
    Action toEntity(ActionDto actionDto);
}
