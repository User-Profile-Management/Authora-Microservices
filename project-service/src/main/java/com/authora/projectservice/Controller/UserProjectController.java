package com.authora.projectservice.Controller;

import com.authora.projectservice.DTO.ApiResponse;
import com.authora.projectservice.DTO.ProfileDTO;
import com.authora.projectservice.DTO.UserProjectDTO;
import com.authora.projectservice.Entity.Project;
import com.authora.projectservice.Entity.UserProject;
import com.authora.projectservice.Service.ProjectService;
import com.authora.projectservice.Service.UserProjectService;
import com.authora.projectservice.Util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-projects")
public class UserProjectController {
    private final UserProjectService userProjectService;
    private final ProjectService projectService;

    @Autowired

    private JwtUtil jwtUtil;

    public UserProjectController(UserProjectService userProjectService, ProjectService projectService) {
        this.userProjectService = userProjectService;
        this.projectService = projectService;
    }

    @GetMapping("/mentor/students")
    @PreAuthorize("hasAuthority('MENTOR')")
    public ResponseEntity<ApiResponse<List<ProfileDTO>>> getStudentsUnderMentor(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(401, "Missing or invalid Authorization header", null, null));
            }

            String token = authHeader.substring(7); // Strip "Bearer " prefix
            String userId = jwtUtil.extractUserId(token); // Extract userId from token

            List<ProfileDTO> students = userProjectService.getStudentsUnderMentor(userId, request);

            return ResponseEntity.ok(new ApiResponse<>(200, "Students fetched successfully", students, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error fetching students", null, e.getMessage()));
        }
    }

    /**
     * Get all user projects (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserProject>>> getAllUserProjects() {
        try {
            List<UserProject> userProjects = userProjectService.getAllUserProjects();
            return ResponseEntity.ok(new ApiResponse<>(200, "Fetched all user projects", userProjects, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error fetching user projects", null, e.getMessage()));
        }
    }

    /**
     * Get assigned projects for a specific user
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MENTOR') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<UserProject>>> getAssignedProjects(
            @PathVariable String userId,
            HttpServletRequest request) {  // Add HttpServletRequest parameter
        try {
            List<UserProject> projects = userProjectService.getAssignedProjects(userId, request);  // Pass both parameters
            return ResponseEntity.ok(new ApiResponse<>(200, "Assigned projects fetched successfully", projects, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Error fetching projects", null, e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MENTOR')")
    public ResponseEntity<ApiResponse<UserProjectDTO>> addUserProject(
            @RequestBody UserProject userProject,
            HttpServletRequest request) {

        try {

            // Get auth token from request
            String authToken = request.getHeader("Authorization");

            // Get the authenticated user ID from the security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(401, "Authentication required", null, "User is not authenticated"));
            }

            // Extract the token (excluding the 'Bearer ' prefix)
            String token = authToken.substring(7);  // "Authorization: Bearer <token>"

            // Extract mentorId from the token (make sure it's present)
            String mentorId = jwtUtil.extractUserId(token);
            if (mentorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(401, "Authentication required", null, "Mentor ID missing in token"));
            }

            // Verify mentor exists
            ProfileDTO mentor = userProjectService.getUserByIdWithToken(mentorId, authToken);
            if (mentor == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse<>(500, "Internal Server Error", null, "Could not retrieve mentor information"));
            }

            // Set the mentorId in the UserProject entity
            userProject.setMentorId(mentorId);

            // Step 1: Check if the project exists
            Project project = projectService.getProjectById(userProject.getProjectId());
            if (project == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "Project not found", null, "The specified project does not exist"));
            }

            // Step 2: Check if the mentor is assigned to the project
            boolean isMentorAssigned = userProjectService.isMentorAssignedToProject(mentorId, userProject.getProjectId());
            if (!isMentorAssigned) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(403, "Access Denied", null, "Mentor is not assigned to this project"));
            }

            // Step 3: Proceed with adding the user to the project
            UserProjectDTO createdUserProject = userProjectService.addUserProject(userProject, request);

            // Return response
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(201, "User project added successfully", createdUserProject, null));
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error adding user to project", null, e.getMessage()));
        }
    }


    /**
     * Update the status of a user's project assignment
     */
    @PutMapping("/users/{userId}/projects/{projectId}")
    @PreAuthorize("hasAuthority('MENTOR')")
    public ResponseEntity<ApiResponse<String>> updateUserProjectStatus(
            @PathVariable String userId,
            @PathVariable Integer projectId,
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        try {
            String mentorId = userDetails.getUsername();
            String authToken = request.getHeader("Authorization");

            // Get mentor profile using userId
            ProfileDTO mentor = userProjectService.getUserByIdWithToken(mentorId, authToken);
            if (mentor == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse<>(500, "Internal Server Error", null, "Could not retrieve mentor information"));
            }

            String status = requestBody.get("status");
            if (status == null || status.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(400, "Bad Request", null, "Status is required"));
            }

            // Step 1: Check if the project exists
            Project project = projectService.getProjectById(projectId);
            if (project == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "Project not found", null, "The specified project does not exist"));
            }

            // Step 2: Check if the mentor is assigned to the project
            boolean isMentorAssigned = userProjectService.isMentorAssignedToProject(mentor.getUserId(), projectId);
            if (!isMentorAssigned) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(403, "Access Denied", null, "Mentor is not assigned to this project"));
            }

            // Step 3: Proceed to update user project status
            userProjectService.updateUserProjectStatus(userId, projectId, status, request);
            return ResponseEntity.ok(new ApiResponse<>(200, "Project status updated successfully", "Status: " + status, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to update status", null, e.getMessage()));
        }
    }

    /**
     * Remove a user from a project (Admin only)
     */
    @DeleteMapping("/users/{userId}/projects/{projectId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<String>> removeUserFromProject(
            @PathVariable String userId,
            @PathVariable Integer projectId,
            HttpServletRequest request) {
        try {
            String authToken = request.getHeader("Authorization");

            // Verify user exists
            ProfileDTO user = userProjectService.getUserByIdWithToken(userId, authToken);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "User not found", null, "User does not exist"));
            }

            // Verify project exists
            Project project = projectService.getProjectById(projectId);
            if (project == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "Project not found", null, "Project does not exist"));
            }

            // Remove user from project
            userProjectService.removeUserFromProject(userId, projectId);

            return ResponseEntity.ok(new ApiResponse<>(200, "User removed from project successfully", null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to remove user from project", null, e.getMessage()));
        }
    }
}