package com.authora.projectservice.DTO;

import com.authora.projectservice.Entity.Project;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {

    private Integer projectId;
    private String projectName;
    private String description;
    private String mentorId;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;

    public ProjectDTO(Project project) {
        this.projectId = project.getProjectId();
        this.projectName = project.getProjectName();
        this.description = project.getDescription();
        this.mentorId = project.getMentorId();
    }

    public ProjectDTO(Integer projectId, String projectName, String description, String mentorId, LocalDateTime deletedAt) {

    }
}