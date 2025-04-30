package com.authora.projectservice.Controller;

import com.authora.projectservice.DTO.ApiResponse;
import com.authora.projectservice.DTO.ProfileDTO;
import com.authora.projectservice.DTO.ProjectDTO;
import com.authora.projectservice.Repository.ProjectRepository;
import com.authora.projectservice.Repository.UserProjectRepository;
import com.authora.projectservice.Service.ProjectService;
import com.authora.projectservice.Util.SecurityContextHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserProjectRepository userProjectRepository;
    private final ProjectRepository projectRepository;

    public ProjectController(ProjectService projectService, UserProjectRepository userProjectRepository, ProjectRepository projectRepository) {
        this.projectService = projectService;
        this.userProjectRepository = userProjectRepository;
        this.projectRepository = projectRepository;
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDTO>> getProjectById(@PathVariable Integer projectId) {
        ProjectDTO project = projectService.getProjectByProjectId(projectId);
        if (project != null) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Project found", project, null));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "Project not found", null, null));
        }
    }

    @PreAuthorize("hasAnyAuthority('MENTOR', 'ADMIN')")
    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDTO>> updateProject(
            @PathVariable Integer projectId,
            @RequestBody ProjectDTO projectDTO,
            @AuthenticationPrincipal UserDetails authenticatedUser,
            HttpServletRequest request) {

        System.out.println("----- Incoming PUT /projects/" + projectId + " -----");
        System.out.println("Received ProjectDTO: " + projectDTO);

        try {
            // Extract the authorization header from the request
            String authToken = request.getHeader("Authorization");
            System.out.println("Authorization header from request: " +
                    (authToken != null ? (authToken.substring(0, Math.min(15, authToken.length())) + "...") : "null"));

            // Fallback to SecurityContext if needed
            if (authToken == null) {
                SecurityContextHelper securityHelper = new SecurityContextHelper();
                authToken = securityHelper.getCurrentAuthToken();
                System.out.println("Retrieved authorization from SecurityContext: " +
                        (authToken != null ? (authToken.substring(0, Math.min(15, authToken.length())) + "...") : "null"));
            }

            // Check authentication
            if (authenticatedUser == null) {
                System.out.println("UserDetails is null. User is not authenticated.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(401, "User is not authenticated", null, "Unauthorized"));
            }

            System.out.println("Authenticated User: " + authenticatedUser.getUsername());
            System.out.println("Calling service to update project...");

            ProjectDTO updatedProject = projectService.updateProject(projectId, projectDTO, authToken);

            System.out.println("Project updated successfully.");
            return ResponseEntity.ok(new ApiResponse<>(200, "Project updated successfully", updatedProject, null));

        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error updating project: " + e.getMessage(), null, e.getMessage()));
        }
    }




    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<String>> deleteProject(@PathVariable Integer projectId) {
        try {
            projectService.deleteProject(projectId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Project deleted successfully.", null, null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null, "Project not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error deleting project: " + e.getMessage(), null, e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/projects")
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getUserProjects(
            @PathVariable String userId,
            HttpServletRequest request) {
        try {
            List<ProjectDTO> userProjects = projectService.getUserProjectsByUserId(userId, request);

            if (userProjects != null && !userProjects.isEmpty()) {
                ApiResponse<List<ProjectDTO>> response = new ApiResponse<>(200, "Projects fetched successfully", userProjects, null);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<List<ProjectDTO>> response = new ApiResponse<>(404, "No projects found for the user", null, null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            ApiResponse<List<ProjectDTO>> response = new ApiResponse<>(500, "Error fetching projects: " + e.getMessage(), null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/users/{userId}/projects/{projectId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUserProject(
            @PathVariable String userId,
            @PathVariable Integer projectId) {
        try {
            projectService.deleteUserProjectForUser(userId, projectId);
            ApiResponse<String> response = new ApiResponse<>(200, "Project deleted successfully for user", "Project ID: " + projectId, null);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(500, "Error deleting project: " + e.getMessage(), null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getAllProjects() {
        try {
            List<ProjectDTO> projects = projectService.getAllProjects();
            ApiResponse<List<ProjectDTO>> response = new ApiResponse<>(200, "Fetched all projects successfully", projects, null);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<List<ProjectDTO>> response = new ApiResponse<>(500, "Error fetching projects: " + e.getMessage(), null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectDTO>> createProject(
            @RequestBody ProjectDTO projectDTO,
            HttpServletRequest request
    ) {
        try {
            // Extract the authorization header from the request
            String authToken = request.getHeader("Authorization");

            // Log for debugging
            System.out.println("Authorization header from request: " +
                    (authToken != null ? (authToken.substring(0, Math.min(15, authToken.length())) + "...") : "null"));

            // If authToken is null, try to get it from SecurityContext as fallback
            if (authToken == null) {
                SecurityContextHelper securityHelper = new SecurityContextHelper();
                authToken = securityHelper.getCurrentAuthToken();
                System.out.println("Retrieved authorization from SecurityContext: " +
                        (authToken != null ? (authToken.substring(0, Math.min(15, authToken.length())) + "...") : "null"));
            }

            // Pass the auth token to the service
            ProjectDTO createdProject = projectService.createProjectByAdmin(projectDTO, authToken);

            ApiResponse<ProjectDTO> response = new ApiResponse<>(201, "Project created successfully", createdProject, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace(); // For detailed error logging
            ApiResponse<ProjectDTO> response = new ApiResponse<>(500, "Error creating project: " + e.getMessage(), null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user/projects")
    public ResponseEntity<List<ProjectDTO>> getProjectsByUserRole(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            List<ProjectDTO> userProjects = projectService.getProjectsByUserRole(email);
            return ResponseEntity.ok(userProjects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/user/projects/count")
    public ResponseEntity<ApiResponse<Integer>> getTotalProjectCount() {
        try {
            Integer totalCount = projectService.getTotalProjectCount();
            ApiResponse<Integer> response = new ApiResponse<>(200, "Total project count fetched successfully", totalCount, null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Integer> response = new ApiResponse<>(500, "Error fetching total project count: " + e.getMessage(), null, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}