package com.bytecoders.emergencyaid;

import com.bytecoders.emergencyaid.openapi.model.LoginUserRequest;
import com.bytecoders.emergencyaid.openapi.model.LoginUserResponse;
import com.bytecoders.emergencyaid.openapi.model.RegisterPatientRequest;
import com.bytecoders.emergencyaid.openapi.model.RegisterUserRequest;
import com.bytecoders.emergencyaid.repository.model.Patient;
import com.bytecoders.emergencyaid.repository.model.User;
import com.bytecoders.emergencyaid.service.PatientService;
import com.bytecoders.emergencyaid.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class contains all the API routes for the system.
 */
@Slf4j
@RestController
public class EmergencyAidController {

  @Autowired
  private UserService userService;

  @Autowired
  private PatientService patientService;

  /**
   * Basic hello endpoint for testing.
   *
   * @return A String
   */
  @GetMapping({"/hello"})
  public String index() {
    return "Hello :)";
  }

  /**
   * Register user endpoint.
   *
   * @param request RegisterUserRequest
   * @return a ResponseEntity with a success message if the operation is successful, or an error
   *     message if the registration is unsuccessful
   */
  @PostMapping({"/register"})
  public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterUserRequest request) {
    try {
      final User user = userService.registerUser(request);
      return new ResponseEntity<>(user, HttpStatus.CREATED);
    } catch (DataIntegrityViolationException e) {
      return new ResponseEntity<>("User already exists for this email", HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Login user endpoint.
   *
   * @param request LoginUserRequest
   * @return a ResponseEntity with a success message if the operation is successful, or an error
   *     message if the login is unsuccessful
   */
  @PostMapping(path = "/login")
  public ResponseEntity<?> loginUser(@RequestBody @Valid LoginUserRequest request) {
    try {
      Optional<LoginUserResponse> jwt = userService.loginUser(request);

      if (jwt.isEmpty()) {
        return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
      }
      return new ResponseEntity<>(jwt.get(), HttpStatus.OK);

    } catch (Exception e) {
      return new ResponseEntity<>("Unexpected error encountered during login",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Register patient endpoint.
   *
   * @param request RegisterPatientRequest
   * @return a ResponseEntity with a success message if the operation is successful, or an error
   *     message if the registration is unsuccessful
   */
  @PostMapping({"/patients/new"})
  public ResponseEntity<?> registerPatient(
      @RequestBody @Valid RegisterPatientRequest request) {
    try {
      final Patient patient = patientService.registerPatient(request);
      return new ResponseEntity<>(patient, HttpStatus.CREATED);
    } catch (DataIntegrityViolationException e) {
      return new ResponseEntity<>("Phone number already registered", HttpStatus.CONFLICT);
    } catch (Exception e) {
      return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Search patients endpoint.
   *
   * @param q the search query. Can be UUID, phone number, or full name
   * @return list of matching patients or an empty list if no matches found
   */
  @GetMapping("/patients/search")
  public ResponseEntity<?> searchPatients(@RequestParam String q) {
    try {
      // search for patients
      List<Patient> patients = patientService.searchPatients(q);
      return new ResponseEntity<>(patients, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>("Something went wrong during the search",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Get patient prescriptions via PharmaId.
   *
   * @param patientId The patient ID
   * @return list of patient's prescriptions, or empty list if no pharmaId or prescriptions exist.
   */
  @GetMapping("/patients/{patientId}/pharmaid/view")
  public ResponseEntity<?> getPatientPrescriptions(@PathVariable UUID patientId) {
    try {
      // logic for null pharmaId or empty prescriptions in PatientService
      List<Map<String, Object>> prescriptions = patientService.getPatientPrescriptions(patientId);
      return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>("Invalid patient ID format", HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("Unexpected error while retrieving prescriptions", e);
      return new ResponseEntity<>("Unexpected error while retrieving prescriptions" + e,
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}