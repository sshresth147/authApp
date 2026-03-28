package com.projlab.auth.auth_app_backend.dtos;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private String message;
    private int status;
    private String error;
    private LocalDateTime timestamp;
}
