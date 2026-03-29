package com.projlab.auth.auth_app_backend.security;

import com.projlab.auth.auth_app_backend.exceptions.ResourceNotFoundException;
import com.projlab.auth.auth_app_backend.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByEmail(username).orElseThrow(()->new ResourceNotFoundException("Invalid username or password!!"));


    }
}
