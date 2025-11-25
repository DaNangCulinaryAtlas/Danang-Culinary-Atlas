package com.atlasculinary.mappers;

import com.atlasculinary.dtos.RoleDto;
import com.atlasculinary.entities.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleDto toDto(Role role);
    Role toEntity(RoleDto roleDto);
}
