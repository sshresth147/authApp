package com.projlab.auth.auth_app_backend.repositories;

import com.projlab.auth.auth_app_backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositories extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
}
