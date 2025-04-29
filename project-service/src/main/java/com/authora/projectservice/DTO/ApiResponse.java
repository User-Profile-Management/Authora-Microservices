package com.authora.projectservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class ApiResponse<T> {
    private int statusCode;
    private String message;
    private T response;
    private String error;

    public ApiResponse(int statusCode, String message, T response, String error) {
        this.statusCode = statusCode;
        this.message = message;
        this.response = response;
        this.error = error;
    }
}

