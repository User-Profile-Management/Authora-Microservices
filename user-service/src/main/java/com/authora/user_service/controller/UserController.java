package com.authora.user_service.controller;

import com.authora.user_service.DTO.ApiResponse;
import com.authora.user_service.DTO.ProfileDTO;
import com.authora.user_service.DTO.RegisterUserDTO;
import com.authora.user_service.Util.JwtUtil;
import com.authora.user_service.modal.Role;
import com.authora.user_service.repository.RoleRepository;
import com.authora.user_service.repository.UserRepository;
import com.authora.user_service.service.UserService;
import com.authora.user_service.modal.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import java.util.*;


    @RestController
    @RequestMapping("/api")
    public class UserController {
        private static final Logger log = LoggerFactory.getLogger(UserController.class);
        private final UserService userService;
        private final JwtUtil jwtUtil;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private  final RoleRepository roleRepository;
        @Autowired
        public UserController(UserService userService, JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
            this.userService = userService;
            this.jwtUtil = jwtUtil;
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
            this.roleRepository = roleRepository;
        }

        @PostMapping("/users/register")
        public ResponseEntity<ApiResponse<?>> registerUser(@RequestBody RegisterUserDTO userDTO) {
            try {
                if (userDTO.getFullName() == null || userDTO.getFullName().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Full Name is required.", null, "Full Name is required."));
                }
                if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Email is required.", null, "Email is required."));
                }
                if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Password is required.", null, "Password is required."));
                }
                if (userDTO.getPassword().length() < 8) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Password must be at least 8 characters long.", null, "Password length is insufficient."));
                }
                if (userDTO.getContactNo() == null || !userDTO.getContactNo().matches("\\d{10}")) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Contact Number must be exactly 10 digits.", null, "Invalid Contact Number format."));
                }
                if (userDTO.getDateOfBirth() == null) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Date of Birth is required.", null, "Date of Birth is required."));
                }
                if (userDTO.getAddress() == null || userDTO.getAddress().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Address is required.", null, "Address is required."));
                }

                if (userDTO.getEmergencyContact() != null && !userDTO.getEmergencyContact().matches("\\d{10}")) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Emergency Contact Number must be exactly 10 digits.", null, "Invalid Emergency Contact format."));
                }

                if (userRepository.existsByEmail(userDTO.getEmail())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new ApiResponse<>(409, "Email is already registered.", null, "Duplicate Email"));
                }


                if (userRepository.existsByContactNo(userDTO.getContactNo())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new ApiResponse<>(409, "Contact number is already registered.", null, "Duplicate Contact Number"));
                }

                User user = new User();
                user.setFullName(userDTO.getFullName());
                user.setDateOfBirth(userDTO.getDateOfBirth());
                user.setPassword(userDTO.getPassword());
                user.setContactNo(userDTO.getContactNo());

                user.setAddress(userDTO.getAddress());
                user.setEmergencyContact(userDTO.getEmergencyContact());
                user.setStatus(User.Status.PENDING);
                user.setProfilePicture(null);
                user.setEmail(userDTO.getEmail());

                if (userDTO.getEmergencyContact() != null && !userDTO.getEmergencyContact().trim().isEmpty()) {
                    user.setEmergencyContact(userDTO.getEmergencyContact());
                }

                User registeredUser = userService.registerUser(user);

                return ResponseEntity.ok(new ApiResponse<>(200, "User registered successfully. Awaiting admin approval.", registeredUser, null));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(500, "Error registering user: " + e.getMessage(), null, e.getMessage()));
            }
        }
        @PreAuthorize("hasAuthority('ADMIN')")
        @GetMapping("/users/count")
        public ResponseEntity<ApiResponse<Long>> getActiveUserCount(@RequestParam String role) {
            long count;
            switch (role.toUpperCase()) {
                case "MENTOR":
                    count = userService.getActiveMentorCount();
                    break;
                case "STUDENT":
                    count = userService.getActiveStudentCount();
                    break;
                default:
                    return ResponseEntity.badRequest().body(
                            new ApiResponse<>(400, "Invalid role. Use 'MENTOR' or 'STUDENT'", null, "Invalid role parameter")
                    );
            }

            return ResponseEntity.ok(new ApiResponse<>(200, "Active " + role + " count retrieved successfully", count, null));
        }

        @GetMapping("/users/profile")
        public ResponseEntity<?> getOwnProfile() {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String currentUserEmail = authentication.getName();

                Optional<User> currentUserOpt = userService.getUserByEmail(currentUserEmail);

                if (currentUserOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse<>(404, "User not found", null, "User does not exist."));
                }

                User user = currentUserOpt.get();
                ProfileDTO profileDTO = new ProfileDTO(
                        user.getUserId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getContactNo(),
                        user.getEmergencyContact(),
                        user.getAddress(),
                        user.getDateOfBirth(),
                        user.getStatus().name(),
                        user.getProfilePicture(),
                        user.getRole().getRoleName(),
                        user.getDeletedAt()
                );


                return ResponseEntity.ok(new ApiResponse<>(200, "Profile retrieved successfully", profileDTO, null));

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse<>(500, "Error retrieving profile", null, e.getMessage()));
            }
        }




        @GetMapping("/users")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'MENTOR')")
        public ResponseEntity<ApiResponse<List<ProfileDTO>>> getUsers(
                @RequestParam(required = false) String role,
                @RequestParam(required = false) String status) {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userRole = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("");

            if (role != null) role = role.toUpperCase();
            if (status != null) status = status.toUpperCase();


            if (userRole.equals("MENTOR") && (role == null || !role.equals("STUDENT"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(403, "Forbidden: Mentors can only view students", null, "Access denied"));
            }


            List<User> users = userService.getUsersByRoleAndStatus(role, status);

            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(new ApiResponse<>(204, "No users found", Collections.emptyList(), null));
            }


            List<ProfileDTO> profileDTOs = users.stream()
                    .map(user -> new ProfileDTO(
                            user.getUserId(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getContactNo(),
                            user.getAddress(),
                            user.getEmergencyContact(),

                            user.getDateOfBirth(),
                            user.getStatus().name(),
                            user.getProfilePicture(),
                            user.getRole().getRoleName(),
                            user.getDeletedAt()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(200, "Profile retrieved successfully", profileDTOs, null));
        }


        @PreAuthorize("hasAuthority('ADMIN')")
        @PutMapping("/users/{userId}/restore")
        public ResponseEntity<ApiResponse<String>> restoreUser(@PathVariable String userId) {
            try {
                userService.restoreUser(userId); // ✅ Uses the correct service method
                return ResponseEntity.ok(new ApiResponse<>(200, "User restored successfully.", null, null));
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, e.getMessage(), null, "User not found"));
            }
        }



        @PutMapping("/users/{userId}")
        public ResponseEntity<ApiResponse<String>> updateUser(
                @PathVariable String userId,
                @RequestBody Map<String, String> request) {
            try {
                User user = userService.getUserById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

                boolean isSoftDeleted = user.getDeletedAt() != null;
                boolean isPendingOrRejected = user.getStatus() == User.Status.PENDING || user.getStatus() == User.Status.REJECTED;

                if (isSoftDeleted) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ApiResponse<>(403, "Cannot update a deleted user.", null, "User is soft-deleted.")
                    );
                }

                if (isPendingOrRejected && request.containsKey("status")) {
                    String status = request.get("status");

                    if (status == null || status.trim().isEmpty()) {
                        return ResponseEntity.badRequest().body(
                                new ApiResponse<>(400, "Status is required.", null, "Status is empty or invalid.")
                        );
                    }

                    try {
                        User.Status newStatus = User.Status.valueOf(status.toUpperCase());
                        if (newStatus == User.Status.PENDING || newStatus == User.Status.REJECTED) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                    new ApiResponse<>(403, "Cannot set status to PENDING or REJECTED.", null, "Invalid status change.")
                            );
                        }
                        user.setStatus(newStatus);

                        // Update role if provided
                        if (request.containsKey("role")) {
                            String roleName = request.get("role");
                            Role role = roleRepository.findByRoleNameIgnoreCase(roleName)
                                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                            user.setRole(role);
                        }

                        user.setUpdatedAt(LocalDateTime.now());
                        userService.saveUser(user);

                        return ResponseEntity.ok(new ApiResponse<>(200, "User status (and role) updated successfully.", null, null));
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(
                                new ApiResponse<>(400, "Invalid status value.", null, "Invalid status provided.")
                        );
                    }
                }

                if (isPendingOrRejected) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ApiResponse<>(403, "Only status can be updated for users with PENDING or REJECTED status.", null, "Update restricted.")
                    );
                }

                if (request.containsKey("status")) {
                    String status = request.get("status");
                    try {
                        User.Status newStatus = User.Status.valueOf(status.toUpperCase());
                        if (newStatus == User.Status.PENDING || newStatus == User.Status.REJECTED) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                    new ApiResponse<>(403, "Cannot set status to PENDING or REJECTED.", null, "Invalid status change.")
                            );
                        }
                        user.setStatus(newStatus);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(
                                new ApiResponse<>(400, "Invalid status value.", null, "Invalid status provided.")
                        );
                    }
                }

                if (request.containsKey("role")) {
                    String roleName = request.get("role");
                    Role role = roleRepository.findByRoleNameIgnoreCase(roleName)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                    user.setRole(role);
                }

                if (request.containsKey("fullName")) {
                    user.setFullName(request.get("fullName"));
                }

                if (request.containsKey("email")) {
                    String newEmail = request.get("email");
                    if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(new ApiResponse<>(409, "Email is already registered.", null, "Duplicate Email"));
                    }
                    user.setEmail(newEmail);
                }

                if (request.containsKey("contactNo")) {
                    String newContactNo = request.get("contactNo");
                    if (!newContactNo.equals(user.getContactNo()) && userRepository.existsByContactNo(newContactNo)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(new ApiResponse<>(409, "Contact number is already registered.", null, "Duplicate Contact Number"));
                    }
                    user.setContactNo(newContactNo);
                }

                user.setUpdatedAt(LocalDateTime.now());
                userService.saveUser(user);

                return ResponseEntity.ok(new ApiResponse<>(200, "User details updated successfully.", null, null));

            } catch (RuntimeException e) {
                log.error("Error updating user: " + e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(

                        new ApiResponse<>(500, "Error updating user details: " + e.getMessage(), null, e.getMessage())
                );
            }
        }



        @GetMapping("/users/profile/{userId}")
        public ResponseEntity<ApiResponse<ProfileDTO>> getUserById(@PathVariable String userId) {
            try {
                User user = userService.getUserById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

                if (user.getDeletedAt() != null) {
                    return ResponseEntity.status(403).body(
                            new ApiResponse<>(403, "This user has been deleted.", null, "Soft-deleted user.")
                    );
                }

                // Create a ProfileDTO with all necessary user details
                ProfileDTO profile = new ProfileDTO(
                        user.getUserId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getContactNo(),
                        user.getEmergencyContact(),
                        user.getAddress(),
                        user.getDateOfBirth(),
                        user.getStatus().name(),
                        user.getProfilePicture(),
                        user.getRole().getRoleName(),
                        user.getDeletedAt()
                );

                return ResponseEntity.ok(new ApiResponse<>(200, "User retrieved successfully.", profile, null));

            } catch (RuntimeException e) {
                return ResponseEntity.status(404).body(
                        new ApiResponse<>(404, "Error retrieving user details: " + e.getMessage(), null, e.getMessage())
                );
            }
        }

        @PutMapping(value = "/users/profile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
        @PreAuthorize("hasAnyAuthority('ADMIN','MENTOR', 'STUDENT')")
        public ResponseEntity<?> updateOwnProfile(

                @RequestParam(value = "emergencyContact",required = false) String emergencyNo,
                @RequestParam(value = "contactNo",required = false) String contactNo,
                @RequestParam(value = "address",required = false) String address,
                @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String currentUserEmail = authentication.getName();
                Optional<User> currentUserOpt = userService.getUserByEmail(currentUserEmail);

                if (currentUserOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body("User not found");
                }

                User user = currentUserOpt.get();

                if (emergencyNo != null && !emergencyNo.isEmpty()) {
                    user.setEmergencyContact(emergencyNo);
                }
                if (contactNo != null && !contactNo.isEmpty()) {
                    user.setContactNo(contactNo);
                }
                if (address != null && !address.isEmpty()) {
                    user.setAddress(address);
                }
                if (profilePicture != null && !profilePicture.isEmpty()) {
                    user.setProfilePicture(profilePicture.getBytes());
                }

                User updatedUser = userService.updateUserProfile(user.getUserId(), user);
                return ResponseEntity.ok(updatedUser);

            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }





        @PreAuthorize("hasAuthority('ADMIN')")
        @DeleteMapping("/users/{id}")
        public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String id) {
            try {
                Optional<User> userOptional = userService.getUserById(id);

                if (userOptional.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse<>(404, "User not found with ID: " + id, null, "User not found"));
                }

                User user = userOptional.get();

                if (user.getDeletedAt() != null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse<>(400, "User is already deleted.", null, "User already deleted"));
                }

                userService.deleteUser(id);
                return ResponseEntity.ok(new ApiResponse<>(200, "User deleted successfully.", null, null));

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse<>(500, "Error deleting user: " + e.getMessage(), null, e.getMessage()));
            }
        }


        @PutMapping("/users/update-password")
        public ResponseEntity<ApiResponse<String>> updatePassword(@RequestBody Map<String, String> request) {
            try {

                String email = getAuthenticatedEmail();


                String currentPassword = request.get("currentPassword");
                String newPassword = request.get("newPassword");


                if (currentPassword == null || newPassword == null || currentPassword.isBlank() || newPassword.isBlank()) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Missing required fields", null, "Old or new password is missing."));
                }


                boolean success = userService.updatePassword(email, currentPassword, newPassword);

                if (success) {
                    return ResponseEntity.ok(new ApiResponse<>(200, "Password updated successfully.", null, null));
                } else {
                    return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Invalid credentials or password update failed.", null, "Password update failed."));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(500, "Error updating password", null, e.getMessage()));
            }
        }
        private String getAuthenticatedEmail() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("User is not authenticated");
            }

            return authentication.getName();
        }


    }


