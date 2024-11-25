package com.bytecoders.emergencyaid.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.bytecoders.emergencyaid.repository.PatientRepository;
import com.bytecoders.emergencyaid.repository.UserRepository;
import com.bytecoders.emergencyaid.repository.model.Patient;
import com.bytecoders.emergencyaid.repository.model.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Tests for {@link ServiceUtils}. */

@ExtendWith(MockitoExtension.class)
public class ServiceUtilsTests {

  @Mock
  private PatientRepository patientRepository;

  @Mock
  private UserRepository userRepository;

  private ServiceUtils serviceUtils;

  private static final UUID MOCK_UUID = UUID.fromString("9101d183-26e6-45b7-a8c4-25f24fdb36fa");
  private Patient patient;
  private User user;

  @BeforeEach
  void setup() {
    serviceUtils = new ServiceUtils();

    // mock patient
    patient = new Patient();
    patient.setId(MOCK_UUID);

    // mock user
    user = new User();
    user.setId(MOCK_UUID);
  }

  @Test
  void findEntityById_Patient_EntityExists() {
    when(patientRepository.findById(MOCK_UUID)).thenReturn(Optional.of(patient));
    Patient result = serviceUtils.findEntityById(MOCK_UUID, "patient", patientRepository);
    assertEquals(patient, result, "findEntityById should return the patient when it exists");
  }

  @Test
  void findEntityById_User_EntityExists() {
    when(userRepository.findById(MOCK_UUID)).thenReturn(Optional.of(user));
    User result = serviceUtils.findEntityById(MOCK_UUID, "user", userRepository);
    assertEquals(user, result, "findEntityById should return the user when it exists");
  }

  @Test
  void findEntityById_Patient_EntityNotFound() {
    when(patientRepository.findById(MOCK_UUID)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> serviceUtils.findEntityById(MOCK_UUID, "patient", patientRepository));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(),
        "Should throw 404 NOT FOUND when patient not found");
    assertEquals("Provided patientId does not exist: " + MOCK_UUID, exception.getReason(),
        "Exception message should match expected message");
  }

  @Test
  void findEntityById_User_EntityNotFound() {
    when(userRepository.findById(MOCK_UUID)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> serviceUtils.findEntityById(MOCK_UUID, "user", userRepository));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(),
        "Should throw 404 NOT FOUND when user not found");
    assertEquals("Provided userId does not exist: " + MOCK_UUID, exception.getReason(),
        "Exception message should match expected message");
  }
}