package com.projlab.auth.auth_app_backend.config;

import com.projlab.auth.auth_app_backend.security.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;


@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {


    @Autowired
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) ->
                        authorize.requestMatchers("/api/v1/auth/register").permitAll()
                                .requestMatchers("/api/v1/auth/login").permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authEx) ->
                        {
                            authEx.printStackTrace();
                            response.setStatus(401);
                            response.setContentType("application/json");
                            String message =  authEx.getMessage();

                            Map<String, String> errorMap = Map.of("message",  message, "statusCode", Integer.toString(401));


                            response.getWriter().write(
                                    objectMapper.writeValueAsString(errorMap) // ✅ correct usage
                            );
                        }

                ))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                //.formLogin(Customizer.withDefaults());


        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder(){

    return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

       return configuration.getAuthenticationManager();
    }


}
