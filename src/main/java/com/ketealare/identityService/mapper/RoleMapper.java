package com.ketealare.identityService.mapper;

import com.ketealare.identityService.dto.request.RoleRequest;
import com.ketealare.identityService.dto.response.RoleResponse;
import com.ketealare.identityService.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
