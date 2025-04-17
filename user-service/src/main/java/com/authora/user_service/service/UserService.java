package com.authora.user_service.service;

import com.authora.user_service.modal.Role;
import com.authora.user_service.modal.User;
import com.authora.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Primary
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getActiveUsersByRole(String roleName) {
        Role role = roleService.getRoleByName(roleName);
        return userRepository.findByRoleAndStatus(role, User.Status.ACTIVE);
    }



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByEmailAndDeletedAtIsNull(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found or has been deleted: " + username);
        }
        User user = userOptional.get();

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority(user.getRole().getRoleName()));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }


    @Transactional
    public User registerUser(User user) {
        Role role = roleService.assignRole("STUDENT"); // always assign STUDENT
        user.setRole(role);
        user.setProfilePicture(null);
        user.setStatus(User.Status.PENDING);
        user.setUserId(generateUserId(role));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public String generateUserId(Role role) {
        String prefix = "USR";
        if (role != null) {
            prefix = switch (role.getRoleName().toUpperCase()) {
                case "STUDENT" -> "USR";
                case "MENTOR" -> "USR";
                default -> prefix;
            };
        }


        Integer count = userRepository.countByRoleAndDeletedAtIsNull(role) + 1;

        String userId = String.format("%s%03d", prefix, count);


        while (userRepository.existsByUserId(userId)) {
            count++;
            userId = String.format("%s%03d", prefix, count);
        }

        return userId;
    }

    @Transactional
    public User createUser(User user) {
        user.setUserId(generateUserId(user.getRole()));
        return userRepository.save(user);
    }

    public Optional<User> loginUser(String email, String rawPassword) {
        Optional<User> user = userRepository.findByEmailAndDeletedAtIsNull(email);

        return user.filter(u ->
                passwordEncoder.matches(rawPassword, u.getPassword()) &&
                        u.getStatus() == User.Status.ACTIVE
        );
    }



    public List<User> getPendingApprovalUsers() {
        return userRepository.findByStatusAndDeletedAtIsNull(User.Status.INACTIVE);
    }



    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAllByDeletedAtIsNull();
    }



    public Long getActiveStudentCount() {
        return userRepository.countByRoleAndStatus("STUDENT", User.Status.ACTIVE);
    }

    public Long getActiveMentorCount() {
        return userRepository.countByRoleAndStatus("MENTOR", User.Status.ACTIVE);
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }



    public User updateUserProfile(String userId, User updatedUser) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .map(existingUser -> {
                    if (updatedUser.getFullName() != null) {
                        existingUser.setFullName(updatedUser.getFullName());
                    }
                    if (updatedUser.getContactNo() != null) {
                        existingUser.setContactNo(updatedUser.getContactNo());
                    }
                    if (updatedUser.getAddress() != null) {
                        existingUser.setAddress(updatedUser.getAddress());
                    }
                    if (updatedUser.getProfilePicture() != null) {
                        existingUser.setProfilePicture(updatedUser.getProfilePicture());
                    }
                    if (updatedUser.getDateOfBirth() != null) {
                        existingUser.setDateOfBirth(updatedUser.getDateOfBirth());
                    }
                    if (updatedUser.getStatus() != null) {
                        existingUser.setStatus(updatedUser.getStatus());
                    }

                    existingUser.setEmergencyContact(updatedUser.getEmergencyContact());
                    existingUser.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(existingUser);
                }).orElseThrow(() -> new RuntimeException("User not found or has been deleted: " + userId));
    }
    public User updateProfilePicture(String userId, MultipartFile profilePicture) throws IOException {
        User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!profilePicture.isEmpty()) {
            user.setProfilePicture(profilePicture.getBytes());
            saveUser(user);
        }
        return user;
    }


    public User updateUserByAdmin(String userId, User updatedUser) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .map(existingUser -> {
                    existingUser.setFullName(updatedUser.getFullName());
                    existingUser.setContactNo(updatedUser.getContactNo());
                    existingUser.setAddress(updatedUser.getAddress());
                    existingUser.setProfilePicture(updatedUser.getProfilePicture());
                    existingUser.setStatus(updatedUser.getStatus());
                    existingUser.setRole(updatedUser.getRole());


                    existingUser.setEmergencyContact(updatedUser.getEmergencyContact());

                    existingUser.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(existingUser);
                }).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    @Transactional
    public void deleteUser(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    public boolean updatePassword(String email, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findByEmailAndDeletedAtIsNull(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();


            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            } else {
                throw new RuntimeException("Incorrect old password.");
            }
        }
        throw new RuntimeException("User not found.");
    }


    public Long getActiveUserCount(String role) {
        return userRepository.countByRoleAndStatus(role, User.Status.ACTIVE);
    }

    public List<User> getPendingApprovalUsersByRole(String role) {
        return userRepository.findByRoleAndStatus(role, User.Status.PENDING);
    }

    public List<User> getUsersByRoleAndStatus(String roleName, String status) {
        Role role = null;
        if (roleName != null) {
            roleName = roleName.toUpperCase();
            role = roleService.getRoleByName(roleName);
        }


        User.Status statusEnum = null;
        if (status != null) {
            try {
                statusEnum = User.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status value: " + status);
            }
        }


        if (role != null && statusEnum != null) {
            return userRepository.findByRoleAndStatus(role, statusEnum);
        } else if (role != null) {
            return userRepository.findByRole(role);
        } else if (statusEnum != null) {
            return userRepository.findByStatus(statusEnum);
        } else {
            return userRepository.findAll();
        }
    }


    public void restoreUser(String userId) {
        Optional<User> userOptional = userRepository.findByIdIncludingDeleted(userId);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        User user = userOptional.get();

        if (user.getDeletedAt() == null) {
            throw new RuntimeException("User is already active.");
        }

        user.setDeletedAt(null);
        userRepository.save(user);
    }








}
