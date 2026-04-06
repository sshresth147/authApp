package com.projlab.auth.auth_app_backend.security;

import com.projlab.auth.auth_app_backend.entities.User;
import com.projlab.auth.auth_app_backend.helpers.UserHelper;
import com.projlab.auth.auth_app_backend.repositories.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private  Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        logger.info("Authorization header : {}",header);
        if (header == null && header.startsWith("Bearer ")) {
            //token extratction and validate with authentication create and then security context
            String token = header.substring(7);

            //check for access token
            if (!jwtService.isAccessToken(token)) {
                //message pass
                filterChain.doFilter(request, response);
                return;
            }


            try {
                Jws<Claims> parse = jwtService.parse(token);
                Claims payload = parse.getPayload();
                String userId = payload.getSubject();
                UUID userUuid = UserHelper.parseUUID(userId);

                userRepository.findById(userUuid)
                        .ifPresent(user -> {
                            // user is found from db
                            //Collection<? extends GrantedAuthority> authorities =  user.getAuthorities();

                            //check if the user is enabled
                            if(user.isEnable()){
                                List<GrantedAuthority> authorities = user.getRoles() == null ? List.of() : user.getRoles().stream()
                                        .map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());

                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                        user.getEmail(), null, authorities
                                );

                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                                //final line to set the authentication to secutity context
                                if(SecurityContextHolder.getContext().getAuthentication()==null)
                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                            }


                        });


            } catch (ExpiredJwtException e) {

                e.printStackTrace();

            } catch (MalformedJwtException e) {

                e.printStackTrace();

            } catch (JwtException e) {

                e.printStackTrace();

            } catch (Exception e) {

                e.printStackTrace();

            }
        }

        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/v1/auth/login");
    }
}
