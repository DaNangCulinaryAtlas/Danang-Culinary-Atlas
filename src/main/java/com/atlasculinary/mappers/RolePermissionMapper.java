package com.atlasculinary.mappers;

import com.atlasculinary.dtos.ActionDto;
import com.atlasculinary.dtos.RolePermissionDto;
import com.atlasculinary.entities.Role;
import com.atlasculinary.entities.RoleActionMap;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RolePermissionMapper {

    @Mapping(target = "roleId", source = "role.roleId")
    @Mapping(target = "roleName", source = "role.roleName")
    @Mapping(target = "description", source = "role.description")
    @Mapping(target = "actions", source = "maps", qualifiedByName = "mapRoleActionMaps")
    RolePermissionDto toDto(Role role, List<RoleActionMap> maps);

    @Named("mapRoleActionMaps")
    default List<ActionDto> mapRoleActionMaps(List<RoleActionMap> maps) {
        if (maps == null) return null;
        return maps.stream()
                .map(this::toActionDto)
                .collect(Collectors.toList());
    }

    @Mapping(source = "action.actionId", target = "actionId")
    @Mapping(source = "action.actionCode", target = "actionCode")
    @Mapping(source = "action.actionName", target = "actionName")
    @Mapping(source = "requiresLicense", target = "requiresLicense")
    ActionDto toActionDto(RoleActionMap map);
}