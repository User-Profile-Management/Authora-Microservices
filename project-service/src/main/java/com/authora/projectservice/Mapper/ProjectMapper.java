package com.authora.projectservice.Mapper;

import com.authora.projectservice.DTO.ProjectDTO;
import com.authora.projectservice.Entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    // Convert Project entity to ProjectDTO
    public ProjectDTO toDTO(Project project) {
        return ProjectDTO.builder()
                .projectId(project.getProjectId())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .mentorId(project.getMentorId())
                .deletedAt(project.getDeletedAt())
                .build();
    }

    // Convert ProjectDTO to Project entity
    public Project toEntity(ProjectDTO projectDTO) {
        Project project = new Project();
        project.setProjectId(projectDTO.getProjectId());
        project.setProjectName(projectDTO.getProjectName());
        project.setDescription(projectDTO.getDescription());
        project.setMentorId(projectDTO.getMentorId());
        project.setDeletedAt(projectDTO.getDeletedAt());
        return project;
    }
}



