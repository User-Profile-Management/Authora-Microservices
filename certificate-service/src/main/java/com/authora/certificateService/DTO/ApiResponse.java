package com.authora.certificateService.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int statusCode;
    private String message;
    private T response;
    private String error;

    public ApiResponse(int i, String certificateUploadedSuccessfully, T response, Object o) {
    }
}
