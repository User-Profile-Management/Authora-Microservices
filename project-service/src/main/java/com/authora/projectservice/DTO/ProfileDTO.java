package com.authora.projectservice.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProfileDTO {
    private String userId;
    private String role;
    private String status;
    private LocalDateTime deletedAt;
}
