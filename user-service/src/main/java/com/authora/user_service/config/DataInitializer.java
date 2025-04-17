package com.authora.user_service.config;

import com.authora.user_service.modal.Role;
import com.authora.user_service.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
@Component
public class DataInitializer {
    private final RoleRepository roleRepository;


    @Autowired
    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;

    }

    @PostConstruct
    public void initializeRoles() {
        createRoleIfNotExists("STUDENT");
        createRoleIfNotExists("MENTOR");
        createRoleIfNotExists("ADMIN");
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByRoleName(roleName) == null) {
            Role role = new Role();
            role.setRoleName(roleName);
            roleRepository.save(role);
        }
    }



}
