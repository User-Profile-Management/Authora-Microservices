package com.authora.projectservice.Service;

import com.authora.projectservice.DTO.ApiResponse;
import com.authora.projectservice.DTO.ProfileDTO;
import com.authora.projectservice.DTO.ProjectDTO;
import com.authora.projectservice.DTO.UserProjectDTO;
import com.authora.projectservice.Entity.Project;
import com.authora.projectservice.Entity.UserProject;
import com.authora.projectservice.Mapper.UserProjectMapper;
import com.authora.projectservice.Repository.ProjectRepository;
import com.authora.projectservice.Repository.UserProjectRepository;
import com.authora.projectservice.Util.SecurityContextHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserProjectService {

    @Value("${service.user-service.url:http://localhost:8081}")
    private String USER_SERVICE_BASE_URL;

    private final UserProjectRepository userProjectRepository;
    private final ProjectRepository projectRepository;
    private final RestTemplate restTemplate;
    private final UserProjectMapper userProjectMapper;
    private final SecurityContextHelper securityContextHelper;

    @Autowired
    public UserProjectService(UserProjectRepository userProjectRepository,
                              ProjectRepository projectRepository,
                              RestTemplate restTemplate,
                              UserProjectMapper userProjectMapper,
                              SecurityContextHelper securityContextHelper) {
        this.userProjectRepository = userProjectRepository;
        this.projectRepository = projectRepository;
        this.restTemplate = restTemplate;
        this.userProjectMapper = userProjectMapper;
        this.securityContextHelper = securityContextHelper;
    }

    // Get all User Projects
    public List<UserProject> getAllUserProjects() {
        return userProjectRepository.findByDeletedAtIsNull();
    }

    // Helper method to extract JWT from HttpServletRequest
    private String extractJwtTokenFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Extract the JWT token
        }
        return null;  // Return null if the token is not present
    }

    // Helper method to get authentication token from security context or request
    private String getAuthToken(HttpServletRequest request) {
        String authToken = null;

        // Try to get token from request first
        if (request != null) {
            authToken = extractJwtTokenFromRequest(request);
            if (authToken != null) {
                authToken = "Bearer " + authToken;
            }
        }

        // Fallback to security context if needed
        if (authToken == null) {
            authToken = securityContextHelper.getCurrentAuthToken();
        }

        return authToken;
    }

    // Helper method to call the User Service via REST API
    public ProfileDTO getUserById(String userId, HttpServletRequest request) {
        String authToken = getAuthToken(request);
        return getUserByIdWithToken(userId, authToken);
    }

    public ProfileDTO getUserByIdWithToken(String userId, String authToken) {
        String url = USER_SERVICE_BASE_URL + "/api/users/profile/" + userId;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<ProfileDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<ProfileDTO>>() {}
            );

            if (response.getBody() != null && response.getBody().getResponse() != null) {
                return response.getBody().getResponse();
            } else {
                System.out.println("Empty or null response from User Service for userId: " + userId);
                return null;
            }
        } catch (Exception ex) {
            System.out.println("Error calling User Service: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

//    // Get user by email from User Service
//    public ProfileDTO getUserByEmail(String email, HttpServletRequest request) {
//        String authToken = getAuthToken(request);
//        String url = USER_SERVICE_BASE_URL + "/api/users/email/" + email;
//
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", authToken);
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//
//            ResponseEntity<ApiResponse<ProfileDTO>> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    entity,
//                    new ParameterizedTypeReference<ApiResponse<ProfileDTO>>() {}
//            );
//
//            if (response.getBody() != null && response.getBody().getResponse() != null) {
//                return response.getBody().getResponse();
//            } else {
//                return null;
//            }
//        } catch (Exception ex) {
//            System.out.println("Error calling User Service: " + ex.getMessage());
//            return null;
//        }
//    }

    // Add a new user project
    @Transactional
    public UserProjectDTO addUserProject(UserProject userProject, HttpServletRequest request) {
        String userId = userProject.getUserId();
        int projectId = userProject.getProjectId();

        // Get auth token
        String authToken = getAuthToken(request);

        // Verify user exists and is active
        ProfileDTO user = getUserByIdWithToken(userId, authToken);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("User " + userId + " is not active. Cannot assign project.");
        }

        // Verify project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));

        // Check if user is already assigned to this project
        if (userProjectRepository.findByUserIdAndProjectIdAndDeletedAtIsNull(userId, projectId).isPresent()) {
            throw new RuntimeException("User is already assigned to this project.");
        }

        // Validate project status
        if (userProject.getStatus() == null) {
            throw new RuntimeException("Project status cannot be null.");
        }

        try {
            userProject.setUserId(userId);
            userProject.setProjectId(projectId);

            UserProject savedUserProject = userProjectRepository.save(userProject);

            ProjectDTO projectDTO = ProjectDTO.builder()
                    .projectId(project.getProjectId())
                    .projectName(project.getProjectName())
                    .description(project.getDescription())
                    .mentorId(project.getMentorId())
                    .deletedAt(project.getDeletedAt())
                    .build();

            return userProjectMapper.mapToUserProjectDTO(savedUserProject, user, projectDTO);
        } catch (Exception e) {
            throw new RuntimeException("Error saving UserProject: " + e.getMessage(), e);
        }
    }

    // Soft delete a User Project
    @Transactional
    public void softDeleteUserProject(int userProjectId) {
        UserProject userProject = userProjectRepository.findById(userProjectId)
                .orElseThrow(() -> new RuntimeException("UserProject not found: " + userProjectId));

        userProject.setDeletedAt(LocalDateTime.now());
        userProjectRepository.save(userProject);
    }

    // Update the status of a UserProject
    @Transactional
    public void updateUserProjectStatus(String userId, Integer projectId, String status, HttpServletRequest request) {
        UserProject userProject = userProjectRepository.findByUserIdAndProjectIdAndDeletedAtIsNull(userId, projectId)
                .orElseThrow(() -> new RuntimeException("User is not assigned to this project"));

        // Verify user exists
        String authToken = getAuthToken(request);
        ProfileDTO user = getUserByIdWithToken(userId, authToken);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        userProject.setStatus(status);
        userProjectRepository.save(userProject);
    }

    // Get students under a specific mentor
    public List<ProfileDTO> getStudentsUnderMentor(String mentorId, HttpServletRequest request) {
        String authToken = getAuthToken(request);

        // First verify the mentor exists
        ProfileDTO mentor = getUserByIdWithToken(mentorId, authToken);
        if (mentor == null) {
            throw new RuntimeException("Mentor not found: " + mentorId);
        }

        if (!"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            throw new RuntimeException("User " + mentorId + " is not a mentor");
        }

        // Find projects managed by this mentor
        List<Project> mentorProjects = projectRepository.findProjectsByMentorId(mentorId);

        if (mentorProjects.isEmpty()) {
            return List.of();
        }

        // Get all project IDs managed by this mentor
        List<Integer> projectIds = mentorProjects.stream()
                .map(Project::getProjectId)
                .collect(Collectors.toList());

        // Find all user-project relationships for these projects
        List<UserProject> userProjects = userProjectRepository.findByProjectIdInAndDeletedAtIsNull(projectIds);

        // Get unique student IDs
        List<String> studentIds = userProjects.stream()
                .map(UserProject::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // Fetch student profiles
        return studentIds.stream()
                .map(id -> getUserByIdWithToken(id, authToken))
                .filter(student -> student != null)
                .collect(Collectors.toList());
    }

    // Get assigned projects for a user
    public List<UserProject> getAssignedProjects(String userId, HttpServletRequest request) {

        // Verify user exists
        String authToken = request.getHeader("Authorization");

        ProfileDTO user = getUserByIdWithToken(userId, authToken);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }

        return userProjectRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    // Check if mentor is assigned to project
    public boolean isMentorAssignedToProject(String mentorId, Integer projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        return project.isPresent() && mentorId.equals(project.get().getMentorId());
    }

    // Remove user from project (soft delete user-project)
    @Transactional
    public void removeUserFromProject(String userId, Integer projectId) {
        UserProject userProject = userProjectRepository.findByUserIdAndProjectIdAndDeletedAtIsNull(userId, projectId)
                .orElseThrow(() -> new RuntimeException("User is not assigned to this project"));

        userProject.setDeletedAt(LocalDateTime.now());
        userProjectRepository.save(userProject);
    }
}