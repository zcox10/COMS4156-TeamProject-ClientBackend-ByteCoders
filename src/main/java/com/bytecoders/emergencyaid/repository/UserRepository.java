package com.bytecoders.emergencyaid.repository;

import com.bytecoders.emergencyaid.repository.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA user repository.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);
}