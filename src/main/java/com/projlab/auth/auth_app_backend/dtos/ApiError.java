package com.projlab.auth.auth_app_backend.dtos;

import java.time.OffsetDateTime;

public record ApiError(
        int status,
        String error,
        String message,
        String path,
        OffsetDateTime timeStamp
) {

    public static ApiError of(int status, String error, String message, String path){
        return  new ApiError(status, error, message, path, OffsetDateTime.now());
    }
}
