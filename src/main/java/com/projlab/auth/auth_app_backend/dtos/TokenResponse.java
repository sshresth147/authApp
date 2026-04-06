package com.projlab.auth.auth_app_backend.dtos;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn,
        String tokenType,
        UserDto user
) {

    public static TokenResponse of( String accessToken, String refreshToken, long expiresIn, UserDto user){

        return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", null);
    }





}
