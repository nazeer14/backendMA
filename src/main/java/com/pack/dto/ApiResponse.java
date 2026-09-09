package com.pack.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiResponse {

    private int status;
    private String message;
    private String timeStamp;

    public static ApiResponse success(String message) {
        return ApiResponse.builder()
                .status(200)
                .message(message)
                .timeStamp(LocalDateTime.now().toString())
                .build();
    }

    public static ApiResponse error(int status, String message) {
        return ApiResponse.builder()
                .status(status)
                .message(message)
                .timeStamp(LocalDateTime.now().toString())
                .build();
    }
}
