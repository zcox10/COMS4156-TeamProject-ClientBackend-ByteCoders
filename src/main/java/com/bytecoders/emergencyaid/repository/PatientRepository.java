package com.bytecoders.emergencyaid.repository;

import com.bytecoders.emergencyaid.repository.model.Patient;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA patient repository.
 */
public interface PatientRepository extends JpaRepository<Patient, UUID> {

  Optional<Patient> findByPhoneNumber(String phoneNumber);

  List<Patient> findByFirstNameIgnoreCase(String firstName);
  
  List<Patient> findByLastNameIgnoreCase(String lastName);

  // find by first and last name, ignore case
  @Query("""
      SELECT p
      FROM Patient p
      WHERE
        LOWER(p.firstName) = LOWER(:firstName)
        AND LOWER(p.lastName) = LOWER(:lastName)
      """)
  List<Patient> findByFullNameIgnoreCase(
      @Param("firstName") String firstName, @Param("lastName") String lastName);
}