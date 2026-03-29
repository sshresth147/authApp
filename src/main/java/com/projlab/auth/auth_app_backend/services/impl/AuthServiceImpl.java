package com.projlab.auth.auth_app_backend.services.impl;

import com.projlab.auth.auth_app_backend.dtos.UserDto;
import com.projlab.auth.auth_app_backend.services.AuthService;
import com.projlab.auth.auth_app_backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserDto registerUser(UserDto userDto) {
        //VERIFY USER,EMAIL AND OTHER DETAILS
        //ROLE ASSIGN A DEFAULT
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

        return userService.createUser(userDto);
    }
}
