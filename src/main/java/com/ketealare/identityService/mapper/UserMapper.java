package com.ketealare.identityService.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.ketealare.identityService.dto.request.UserCreationRequest;
import com.ketealare.identityService.dto.request.UserUpdateRequest;
import com.ketealare.identityService.dto.response.UserResponse;
import com.ketealare.identityService.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
