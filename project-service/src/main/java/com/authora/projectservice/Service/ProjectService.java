package com.authora.projectservice.Service;

import com.authora.projectservice.DTO.ApiResponse;
import com.authora.projectservice.DTO.ProfileDTO;
import com.authora.projectservice.DTO.ProjectDTO;
import com.authora.projectservice.Entity.Project;
import com.authora.projectservice.Entity.UserProject;
import com.authora.projectservice.Mapper.ProjectMapper;
import com.authora.projectservice.Repository.ProjectRepository;
import com.authora.projectservice.Repository.UserProjectRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import com.authora.projectservice.Util.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final String USER_SERVICE_BASE_URL = "http://localhost:8081"; // Set the correct URL

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserProjectRepository userProjectRepository;
    private final RestTemplate restTemplate;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMapper projectMapper,
                          UserProjectRepository userProjectRepository,
                          RestTemplate restTemplate) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userProjectRepository = userProjectRepository;
        this.restTemplate = restTemplate;
    }


    public ProfileDTO getUserByIdFromUserService(String userId, String authToken) {
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
                return response.getBody().getResponse(); // Extract the actual ProfileDTO from the response
            } else {
                System.out.println("Empty or null response from User Service for userId: " + userId);
                return null;
            }
        } catch (Exception ex) {
            System.out.println("Error calling User Service: " + ex.getMessage());
            ex.printStackTrace(); // For debugging
            return null;
        }
    }


    // Overloaded method that extracts the JWT from HttpServletRequest
    public ProfileDTO getUserByIdFromUserService(String userId, HttpServletRequest request) {
        // Extract JWT token from the HttpServletRequest
        String authToken = extractJwtTokenFromRequest(request);

        if (authToken != null) {
            authToken = "Bearer " + authToken;
            System.out.println("Using JWT Token from request: " + authToken);
        } else {
            // Get token from current security context as fallback
            authToken = getCurrentAuthToken();
            System.out.println("Using JWT Token from security context: " + authToken);
        }

        return getUserByIdFromUserService(userId, authToken);
    }

    @Autowired
    private SecurityContextHelper securityContextHelper;

    // Helper method to get current authentication token from security context
    private String getCurrentAuthToken() {
        return securityContextHelper.getCurrentAuthToken();
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

    // Method to call User Service's REST API to get user details by email
    public ProfileDTO getUserByEmailFromUserService(String email) {
        // Get current authentication header
        String authToken = getCurrentAuthToken();

        String url = USER_SERVICE_BASE_URL + "/email/" + email;
        try {
            // Send GET request with Authorization header
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Perform the request and return the response as ProfileDTO
            return restTemplate.exchange(url, HttpMethod.GET, entity, ProfileDTO.class).getBody();
        } catch (Exception ex) {
            System.out.println("Error calling User Service: " + ex.getMessage());
            return null;
        }
    }

    public ProjectDTO getProjectByProjectId(Integer projectId) {
        return projectRepository.findById(projectId)
                .filter(project -> project.getDeletedAt() == null)
                .map(projectMapper::toDTO)
                .orElse(null);
    }
    public ProjectDTO updateProject(Integer projectId, ProjectDTO projectDTO, String authToken) {
        System.out.println("----- Service: updateProject -----");
        System.out.println("Project ID: " + projectId);
        System.out.println("Auth Token: " + (authToken != null ? (authToken.substring(0, Math.min(15, authToken.length())) + "...") : "null"));
        System.out.println("Incoming DTO: " + projectDTO);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    System.out.println("Project not found with ID: " + projectId);
                    return new RuntimeException("Project not found with ID: " + projectId);
                });

        System.out.println("Existing Project Before Update: " + project);

        // Update project details
        project.setProjectName(projectDTO.getProjectName());
        project.setDescription(projectDTO.getDescription());

        // Save updated project
        Project savedProject = projectRepository.save(project);
        System.out.println("Updated Project: " + savedProject);

        // Convert to DTO
        ProjectDTO updatedDTO = projectMapper.toDTO(savedProject);
        System.out.println("Returning DTO: " + updatedDTO);

        return updatedDTO;
    }




    public List<ProjectDTO> getUserProjectsByUserId(String userId, HttpServletRequest request) {
        // Fetch the user from UserService via REST API
        ProfileDTO user = getUserByIdFromUserService(userId, request);
        if (user == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        // Fetch projects assigned to the user
        List<Project> userProjects = projectRepository.findProjectsByUserId(userId);

        // If projects are found, map them to DTOs, else return an empty list
        return userProjects.stream()
                .map(project -> new ProjectDTO(project))
                .collect(Collectors.toList());
    }

    public ProjectDTO createProjectByAdmin(ProjectDTO projectDTO, String authToken) {
        // Check if mentor ID is provided
        if (projectDTO.getMentorId() == null) {
            throw new RuntimeException("Mentor ID is required.");
        }

        // Make sure authToken starts with "Bearer "
        if (authToken != null && !authToken.startsWith("Bearer ")) {
            authToken = "Bearer " + authToken;
        }

        // Verify if mentor exists via UserService REST API using the provided token
        ProfileDTO mentor = getUserByIdFromUserService(projectDTO.getMentorId(), authToken);

        if (mentor == null) {
            throw new RuntimeException("Mentor not found with ID: " + projectDTO.getMentorId());
        }

        // Log the mentor profile details for debugging
        System.out.println("Retrieved mentor details: " + mentor);

        // Ensure mentor has the "MENTOR" role
        if (!"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            throw new RuntimeException("User " + mentor.getUserId() + " is not a mentor.");
        }

        // Ensure the mentor is active and not deleted
        if (!"ACTIVE".equalsIgnoreCase(mentor.getStatus()) || mentor.getDeletedAt() != null) {
            throw new RuntimeException("Mentor " + mentor.getUserId() + " is inactive or deleted.");
        }

        // Set mentor ID in the ProjectDTO
        projectDTO.setMentorId(mentor.getUserId());

        // Use ProjectMapper to convert ProjectDTO to Project entity
        Project project = projectMapper.toEntity(projectDTO);

        // Save the project entity in the repository
        project = projectRepository.save(project);

        // Convert the saved Project entity back to ProjectDTO and return it
        return projectMapper.toDTO(project);
    }

    public List<ProjectDTO> getProjectsByUserRole(String email) {
        // Get user from UserService
        ProfileDTO user = getUserByEmailFromUserService(email);
        if (user == null) {
            throw new RuntimeException("User not found.");
        }

        // Fetch the projects based on user role
        List<Project> projects;
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            projects = projectRepository.findAllProjects();  // Admin can view all projects
        } else if ("MENTOR".equalsIgnoreCase(user.getRole())) {
            projects = projectRepository.findProjectsByMentorId(user.getUserId());  // Mentor can view their own projects
        } else {
            throw new RuntimeException("Invalid role or access not allowed.");
        }

        // Convert the List<Project> to List<ProjectDTO> using ProjectMapper
        return projects.stream()
                .map(projectMapper::toDTO)  // Use ProjectMapper to convert each Project to ProjectDTO
                .collect(Collectors.toList());  // Collect the result as a List<ProjectDTO>
    }

    public Project getProjectById(Integer projectId) {
        return projectRepository.findById(projectId).orElse(null);
    }

    public List<ProjectDTO> getAllProjects() {
        List<Project> projects = projectRepository.findByDeletedAtIsNull();
        return projects.stream().map(projectMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        boolean projectExists = projectRepository.existsByProjectName(projectDTO.getProjectName());

        if (projectExists) {
            throw new RuntimeException("Project with name '" + projectDTO.getProjectName() + "' already exists.");
        }

        // Get the current auth token from security context
        String authToken = getCurrentAuthToken();

        // Validate mentor exists via UserService REST API
        ProfileDTO mentor = getUserByIdFromUserService(projectDTO.getMentorId(), authToken);
        if (mentor == null) {
            throw new RuntimeException("Mentor not found with ID: " + projectDTO.getMentorId());
        }

        if (!"MENTOR".equalsIgnoreCase(mentor.getRole())) {
            throw new RuntimeException("User " + mentor.getUserId() + " is not a mentor.");
        }

        if (!"ACTIVE".equalsIgnoreCase(mentor.getStatus()) || mentor.getDeletedAt() != null) {
            throw new RuntimeException("Mentor " + mentor.getUserId() + " is inactive or deleted.");
        }

        Project project = Project.builder()
                .projectName(projectDTO.getProjectName())
                .description(projectDTO.getDescription())
                .mentorId(projectDTO.getMentorId())
                .build();

        project = projectRepository.save(project);

        return projectMapper.toDTO(project);
    }

    public Integer getTotalProjectCount() {
        return (int) projectRepository.findByDeletedAtIsNull().size();
    }

    public void deleteProject(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));

        if (project.getDeletedAt() != null) {
            throw new RuntimeException("Project is already deleted.");
        }

        project.setDeletedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    public ProjectDTO getProjectDetails(Integer projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));
        return projectMapper.toDTO(project);
    }

    public void deleteUserProjectForUser(String userId, Integer projectId) {
    }

    public boolean isMentorAssignedToProject(String userId, Integer projectId) {
        // Check if there's a UserProject entry with the given userId and projectId
        Optional<UserProject> userProject = userProjectRepository.findByUserIdAndProjectId(userId, projectId);

        // Return true if such a record exists, otherwise return false
        return userProject.isPresent();
    }
}