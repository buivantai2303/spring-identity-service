package com.ketealare.identityService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ketealare.identityService.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {}
