package com.authora.user_service.repository;

import com.authora.user_service.modal.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    Optional<Role> findByRoleNameIgnoreCase(String roleName);
}