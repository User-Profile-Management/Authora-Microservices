package com.authora.user_service.controller;

import com.authora.user_service.modal.Role;
import com.authora.user_service.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }


    @GetMapping("/assign")
    public ResponseEntity<Role> assignRole(@RequestParam String roleName) {
        return ResponseEntity.ok(roleService.assignRole(roleName));
    }


    @GetMapping("/{roleId}")
    public ResponseEntity<Optional<Role>> getRoleById(@PathVariable Integer roleId) {
        return ResponseEntity.ok(roleService.getRoleById(roleId));
    }




    @GetMapping("/all")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

}
