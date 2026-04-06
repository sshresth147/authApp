package com.projlab.auth.auth_app_backend.security;

import com.projlab.auth.auth_app_backend.entities.Role;
import com.projlab.auth.auth_app_backend.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Getter
@Setter
public class JwtService {

private final SecretKey key;
private final long accessTtlSeconds;
private final long refreshTtlSeconds;
private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
            @Value("${security.jwt.issuer}")String issuer) {


        if(secret==null|| secret.length()<64){
            throw new IllegalArgumentException("inavlid secret key");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }


    //generate token


    public String generateAccessToken(User user) {
        Instant now = Instant.now(); // Get current timestamp (token issue time)

        // Extract roles from user; if null, use empty list to avoid NullPointerException
        List<String> roles = user.getRoles() == null ? List.of() :
                user.getRoles().stream()
                        .map(Role::getName) // Convert each Role object to its name (String)
                        .toList(); // Collect into a List<String>

        return Jwts.builder() // Start building the JWT
                .id(UUID.randomUUID().toString()) // Unique token ID (jti) for tracking/revocation
                .subject(user.getId().toString()) // Set subject (usually user ID)
                .issuer(issuer) // Set token issuer (your application name)
                .issuedAt(Date.from(now)) // Set issued-at time
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds))) // Set expiration time
                .claims(Map.of( // Add custom claims (payload data)
                        "email", user.getEmail(), // Store user's email
                        "roles", roles, // Store user roles
                        "typ", "access" // Custom type to distinguish access token
                ))
                .signWith(key, SignatureAlgorithm.HS512) // Sign token with secret key using HS512 algo
                .compact(); // Build and serialize JWT into a String
    }

  /// generate refresh token
    public String generateRefreshToken(User user, String jti) {
        Instant now = Instant.now(); // Current time (token issue time)

        return Jwts.builder() // Start building the JWT
                .id(jti) // if the user is passed with id then we will only generate the token not the id

                .subject(user.getId().toString()) // Set subject (user ID)
                .issuer(issuer) // Set issuer (your application name)
                .issuedAt(Date.from(now)) // Set issued-at time
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds))) // Longer expiration for refresh token
                .claims(Map.of( // Add minimal claims (keep refresh tokens lightweight)
                        "typ", "refresh" // Identify this as a refresh token
                ))
                .signWith(key, SignatureAlgorithm.HS512) // Sign with same secret key
                .compact(); // Build and return JWT string
    }



    //parse the token
    public Jws<Claims> parse(String token) {
        try {
            return Jwts.parser() // Create JWT parser
                    .verifyWith(key) // Set the same secret key used for signing
                    .build() // Build the parser
                    .parseSignedClaims(token) ;// Parse and validate the token (signature + structure)


        } catch (JwtException e) {
            // Token is valid but expired
            throw e;
        }
    }



    public boolean isAccessToken(String token){
        Claims c = parse(token).getPayload();
        return "access".equals(c.get("typ"));
    }

    public UUID getUserId(String token){
        Claims c = parse(token).getPayload();
        return UUID.fromString(c.getSubject());
    }


    public String getJti(String token){
        return parse(token).getPayload().getId();
    }

    public List <String> getRoles(String token){
        Claims c = parse(token).getPayload();
        return (List<String>) c.get("roles");
    }

    public String getEmail(String token){
        Claims c = parse(token).getPayload();
        return (String) c.get("email");
    }
}
