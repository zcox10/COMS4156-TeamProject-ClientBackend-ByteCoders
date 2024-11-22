package com.bytecoders.emergencyaid.repository.model;

import com.bytecoders.emergencyaid.util.QueryValidator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

/**
 * Model class to describe the "patients" table.
 */
@Data
@Entity
@Table(name = "patients")
public class Patient {

  @Id
  @UuidGenerator
  @JsonProperty
  @Column(name = "patient_id", columnDefinition = "UUID")
  private UUID id;

  @Column(name = "first_name", nullable = false)
  @NotBlank
  private String firstName;

  @Column(name = "last_name", nullable = false)
  @NotBlank
  private String lastName;

  @Column(name = "phone_number", nullable = false)
  @Pattern(regexp = QueryValidator.PHONE_NUMBER_REGEX,
      message = "Phone number must be 10 digits with dash separators")
  private String phoneNumber;

  @Column(name = "patient_pharma_id")
  @JsonProperty
  private String pharmaId;
}