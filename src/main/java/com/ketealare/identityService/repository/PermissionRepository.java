package com.ketealare.identityService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ketealare.identityService.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {}
