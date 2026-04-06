package com.projlab.auth.auth_app_backend.controllers;


import com.projlab.auth.auth_app_backend.dtos.LoginRequest;
import com.projlab.auth.auth_app_backend.dtos.TokenResponse;
import com.projlab.auth.auth_app_backend.dtos.UserDto;
import com.projlab.auth.auth_app_backend.entities.User;
import com.projlab.auth.auth_app_backend.repositories.UserRepository;
import com.projlab.auth.auth_app_backend.security.JwtService;
import com.projlab.auth.auth_app_backend.services.AuthService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private  final ModelMapper mapper;


    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login (@RequestBody LoginRequest loginRequest){

        //authenticte the user first
        Authentication authentication = authenticate(loginRequest);
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new BadCredentialsException("Invalid username AND PASSWORD"));

        if(!user.isEnabled()){
            throw new DisabledException("User is Disabled");
        }

        //generate the token here now

        String accessToken = jwtService.generateAccessToken(user);
        TokenResponse tokenResponse = TokenResponse.of(accessToken,"",jwtService.getAccessTtlSeconds(),mapper.map(user , UserDto.class));

        return ResponseEntity.ok(tokenResponse);
    }

    private Authentication authenticate(LoginRequest loginRequest) {

        try{
           return  authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        }catch(Exception e){
            throw new BadCredentialsException("username password not valid");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser (@RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }
}
