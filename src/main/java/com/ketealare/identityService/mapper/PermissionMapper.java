package com.ketealare.identityService.mapper;

import org.mapstruct.Mapper;

import com.ketealare.identityService.dto.request.PermissionRequest;
import com.ketealare.identityService.dto.response.PermissionResponse;
import com.ketealare.identityService.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
