package com.authora.projectservice.Mapper;

import com.authora.projectservice.DTO.ProfileDTO;
import com.authora.projectservice.DTO.ProjectDTO;
import com.authora.projectservice.DTO.UserProjectDTO;
import com.authora.projectservice.Entity.UserProject;
import com.authora.projectservice.Service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserProjectMapper {

    private final ProjectService projectService;

    @Autowired
    public UserProjectMapper(ProjectService projectService) {
        this.projectService = projectService;
    }

    public UserProjectDTO mapToUserProjectDTO(UserProject userProject, HttpServletRequest request) {
        // Validate the userProject before fetching related data
        if (userProject == null) {
            throw new IllegalArgumentException("UserProject cannot be null.");
        }

        // Fetch user details from user service (assuming userProject has getUserId method)
        ProfileDTO profileDTO = projectService.getUserByIdFromUserService(userProject.getUserId(), request);
        if (profileDTO == null) {
            throw new RuntimeException("Profile not found for userId: " + userProject.getUserId());
        }

        // Fetch the associated project data using projectId
        ProjectDTO projectDTO = projectService.getProjectByProjectId(userProject.getProjectId());
        if (projectDTO == null) {
            throw new RuntimeException("Project not found for UserProject ID: " + userProject.getId());
        }

        // Return the final UserProjectDTO
        return new UserProjectDTO(
                userProject.getId(),
                profileDTO,
                projectDTO,
                userProject.getStatus(),
                userProject.getMentorId()
        );
    }

    // Overloaded method when ProfileDTO and ProjectDTO are already provided
    public UserProjectDTO mapToUserProjectDTO(UserProject userProject, ProfileDTO profileDTO, ProjectDTO projectDTO) {
        if (userProject == null) {
            throw new IllegalArgumentException("UserProject cannot be null.");
        }

        // Ensure the provided ProfileDTO and ProjectDTO are valid
        if (profileDTO == null) {
            throw new IllegalArgumentException("ProfileDTO cannot be null.");
        }

        if (projectDTO == null) {
            throw new IllegalArgumentException("ProjectDTO cannot be null.");
        }

        return new UserProjectDTO(
                userProject.getId(),
                profileDTO,
                projectDTO,
                userProject.getStatus(),
                userProject.getMentorId()
        );
    }
}
