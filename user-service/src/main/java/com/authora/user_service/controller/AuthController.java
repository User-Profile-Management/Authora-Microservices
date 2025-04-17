package com.authora.user_service.controller;
import com.authora.user_service.modal.User;
import com.authora.user_service.DTO.ApiResponse;
import com.authora.user_service.DTO.JWTResponse;
import com.authora.user_service.DTO.LoginRequestDTO;
import com.authora.user_service.modal.User;

import com.authora.user_service.service.AuthService;
import com.authora.user_service.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.authora.user_service.modal.User.Status.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, UserService userService) {

        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JWTResponse>> login(@RequestBody LoginRequestDTO loginRequest) {
        try {

            Optional<User> userOptional = userService.getUserByEmail(loginRequest.getEmail());

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new ApiResponse<>(401, "Invalid email or password.", null, "Authentication failed.")
                );
            }

            User user = userOptional.get();

            if (user.getDeletedAt() != null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>(403, "Your account has been deleted.", null, "Soft-deleted user.")
                );
            }


            switch (user.getStatus()) {
                case PENDING:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ApiResponse<>(403, "Your account is pending approval.", null, "User status: PENDING.")
                    );
                case REJECTED:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ApiResponse<>(403, "Your account has been rejected.", null, "User status: REJECTED.")
                    );
                case INACTIVE:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ApiResponse<>(403, "Your account is inactive. Please contact support.", null, "User status: INACTIVE.")
                    );
                case ACTIVE:

                    JWTResponse jwtResponse = authService.authenticateUser(loginRequest);
                    return ResponseEntity.ok(
                            new ApiResponse<>(200, "Login successful.", jwtResponse, null)
                    );
                default:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ApiResponse<>(403, "Invalid account status.", null, "Unknown status.")
                    );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(500, "Error during login.", null, e.getMessage())
            );
        }
    }
    @PostMapping("/google-login")
    public ResponseEntity<ApiResponse<JWTResponse>> googleLogin(@RequestHeader("X-Google-ID-Token") String idToken) {
        try {
            // 1. Verify the Google ID token using Firebase or Google's public key
            String email = authService.verifyGoogleTokenAndGetEmail(idToken);

            // 2. Check if user exists in DB
            Optional<User> userOptional = userService.getUserByEmail(email);
            System.out.println("✅ User found? " + userOptional.isPresent());
            userOptional.ifPresent(u -> System.out.println("✅ User status: " + u.getStatus()));

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new ApiResponse<>(401, "No account found with this Google email. Please register first.", null, "User not found.")
                );
            }

            User user = userOptional.get();

            if (user.getDeletedAt() != null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>(403, "Your account has been deleted.", null, "Soft-deleted user.")
                );
            }

            switch (user.getStatus()) {
                case PENDING:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ApiResponse<>(403, "Your account is pending approval.", null, "User status: PENDING.")
                    );
                case REJECTED:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ApiResponse<>(403, "Your account has been rejected.", null, "User status: REJECTED.")
                    );
                case INACTIVE:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ApiResponse<>(403, "Your account is inactive. Please contact support.", null, "User status: INACTIVE.")
                    );
                case ACTIVE:
                    // 3. Generate backend JWT token
                    JWTResponse jwtResponse = authService.generateJwtToken(user);
                    return ResponseEntity.ok(
                            new ApiResponse<>(200, "Google login successful.", jwtResponse, null)
                    );
                default:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ApiResponse<>(403, "Invalid user status.", null, "Invalid status.")
                    );
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(401, "Google token verification failed.", null, e.getMessage() != null ? e.getMessage() : "Unknown error")
            );
        }
    }

}

