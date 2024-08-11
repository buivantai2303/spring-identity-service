package com.ketealare.identityService.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ketealare.identityService.dto.request.RoleRequest;
import com.ketealare.identityService.dto.response.RoleResponse;
import com.ketealare.identityService.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
