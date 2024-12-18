package com.bytecoders.emergencyaid.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

/**
 * Model class to describe the "users" table.
 */
@Data
@Entity
@Table(name = "users")
public class User {

  @Id
  @UuidGenerator
  @JsonProperty
  @Column(name = "user_id", columnDefinition = "UUID")
  private UUID id;

  @Column(name = "email", nullable = false, unique = true)
  @JsonProperty
  private String email;

  @Column(name = "hashed_password", nullable = false)
  @JsonIgnore
  private String hashedPassword;
}