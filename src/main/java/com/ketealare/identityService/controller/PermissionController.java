package com.ketealare.identityService.controller;

import com.ketealare.identityService.dto.request.PermissionRequest;
import com.ketealare.identityService.dto.response.ApiResponse;
import com.ketealare.identityService.dto.response.PermissionResponse;
import com.ketealare.identityService.service.PermissionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/permissions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {

    PermissionService permissionService;

    @PostMapping
    ApiResponse<PermissionResponse> addPermission(@RequestBody @Valid PermissionRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<PermissionResponse>> getPermissions() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getPermission())
                .build();
    }

    @DeleteMapping("/{permissionId}")
    ApiResponse<String> deletePermission(@PathVariable String permissionId) {
        permissionService.deletePermission(permissionId);
        return ApiResponse.<String>builder()
                .result("Permission has been deleted successfully")
                .build();
    }
}
