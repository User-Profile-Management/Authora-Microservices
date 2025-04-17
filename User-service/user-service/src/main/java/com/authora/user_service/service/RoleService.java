package com.authora.user_service.service;

import com.authora.user_service.modal.Role;
import com.authora.user_service.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    public Role assignRole(String roleName) {
        Role role = roleRepository.findByRoleName(roleName);
        if (role == null) {
            throw new RuntimeException("Role not found: " + roleName);
        }
        return role;
    }


    public Optional<Role> getRoleById(Integer roleId) {
        return roleRepository.findById(roleId);
    }





    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }


    public Role createRole(Role role) {
        return roleRepository.save(role);
    }
    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleNameIgnoreCase(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
    }

}
