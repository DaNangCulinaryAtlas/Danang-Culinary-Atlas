package com.atlasculinary.mappers;

import com.atlasculinary.dtos.AddRoleRequest;
import com.atlasculinary.dtos.RoleDto;
import com.atlasculinary.dtos.UpdateRoleRequest;
import com.atlasculinary.entities.Role;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleMapper {

    RoleDto toDto(Role role);

    Role toEntity(RoleDto roleDto);

    @Mapping(target = "roleId", ignore = true)
    Role toEntity(AddRoleRequest request);


    @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "roleName", ignore = true)
    void updateEntityFromRequest(UpdateRoleRequest request, @MappingTarget Role entity);

    List<RoleDto> toDtoList(List<Role> roles);

    @AfterMapping
    default void toUpperCaseRoleName(@MappingTarget Role role) {
        if (role.getRoleName() != null) {
            role.setRoleName(role.getRoleName().toUpperCase());
        }
    }
}