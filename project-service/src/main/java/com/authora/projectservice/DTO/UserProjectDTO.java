package com.authora.projectservice.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProjectDTO {
    private int id;
    private ProfileDTO profile;
    private ProjectDTO project;
    private String status;
    private String mentorId;
}
