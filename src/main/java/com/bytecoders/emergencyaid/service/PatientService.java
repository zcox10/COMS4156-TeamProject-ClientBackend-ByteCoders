package com.bytecoders.emergencyaid.service;

import com.bytecoders.emergencyaid.openapi.model.RegisterPatientRequest;
import com.bytecoders.emergencyaid.repository.PatientRepository;
import com.bytecoders.emergencyaid.repository.model.Patient;
import com.bytecoders.emergencyaid.util.QueryValidator;
import com.bytecoders.emergencyaid.util.ServiceUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service operations around {@link Patient}.
 */
@Slf4j
@Service
public class PatientService {

  @Autowired
  private PatientRepository patientRepository;

  @Autowired
  private ServiceUtils serviceUtils;

  @Autowired
  private QueryValidator queryValidator;

  @Autowired
  private PharmaidAuthService pharmadAuthService;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${pharmaid.api.base-url}")
  private String pharmaidBaseUrl;

  @Value("${pharmaid.api.pharmaid}")
  private String clientPharmaId;

  /**
   * Register new patient service.
   *
   * @param registerPatientRequest request
   * @return Patient the newly created patient
   */
  public Patient registerPatient(RegisterPatientRequest registerPatientRequest) {
    final String phoneNumber = queryValidator.safeTrim(registerPatientRequest.getPhoneNumber());
    final String firstName = queryValidator.safeTrim(registerPatientRequest.getFirstName());
    final String lastName = queryValidator.safeTrim(registerPatientRequest.getLastName());
    final String pharmaId = queryValidator.safeTrim(registerPatientRequest.getPharmaId());

    if (!queryValidator.isPhoneNumber(phoneNumber)) {
      throw new IllegalArgumentException("Phone number must be 10 digits with dash separators");
    }
    if (!queryValidator.isName(firstName)) {
      throw new IllegalArgumentException("First name is not a valid format");
    }
    if (!queryValidator.isName(lastName)) {
      throw new IllegalArgumentException("Last name is not a valid format");
    }

    final Patient newPatient = new Patient();
    newPatient.setFirstName(firstName);
    newPatient.setLastName(lastName);
    newPatient.setPhoneNumber(phoneNumber);
    newPatient.setPharmaId(pharmaId);
    return patientRepository.save(newPatient);
  }

  /**
   * Retrieve patient prescriptions from PharmaId.
   *
   * @param patientId the UUID of patient account
   * @return a list of patient's prescriptions
   */
  public List<Map<String, Object>> getPatientPrescriptions(UUID patientId) {
    // check if PharmaId account exists
    if (!hasPharmaId(patientId)) {
      return Collections.emptyList();
    }

    // request access to VIEW patient prescriptions
    String pharmaId = getPatient(patientId).getPharmaId();
    if (!requestPatientPrescriptionAccess(pharmaId)) {
      throw new IllegalStateException("Unable to obtain VIEW access for prescriptions");
    }

    try {
      // PharmaId getPrescriptions endpoint
      String url = String.format("%s/users/%s/prescriptions", pharmaidBaseUrl, pharmaId);

      // HttpEntity with headers only
      HttpEntity<Void> entity = new HttpEntity<>(pharmadAuthService.getHeaders());

      return restTemplate.exchange(url, HttpMethod.GET, entity,
          new ParameterizedTypeReference<List<Map<String, Object>>>() {
          }).getBody();
    } catch (Exception e) {
      throw new IllegalStateException("Error while retrieving prescriptions from PharmaId", e);
    }
  }

  /**
   * Requests access to VIEW a patient's prescriptions.
   *
   * @param pharmaId the ID of the patient's PharmaId account
   * @return boolean if the request was successful or not
   */
  public boolean requestPatientPrescriptionAccess(String pharmaId) {
    try {
      // create a VIEW access request
      String url = String.format("%s/users/%s/requests?requesterId=%s", pharmaidBaseUrl, pharmaId,
          clientPharmaId);
      log.debug("Constructed URL: {}", url);

      // construct HttpEntity with headers and body
      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("sharePermissionType", "VIEW");
      HttpEntity<Map<String, String>> entity =
          new HttpEntity<>(requestBody, pharmadAuthService.getHeaders());

      // create POST request
      ResponseEntity<?> response = restTemplate.postForEntity(url, entity, Object.class);

      log.debug("Response: Status Code = {}, Body = {}", response.getStatusCode(),
          response.getBody());

      // ensure 201 response
      return response.getStatusCode() == HttpStatus.CREATED;
    } catch (Exception e) {
      throw new IllegalStateException(
          "Error while requesting access to prescriptions: " + e.getMessage(), e);
    }
  }

  /**
   * Provide a query to search for patients by UUID, phone number, or full name.
   *
   * @param query input a UUID, phone number, or patient full name
   * @return list of patients matching the query criteria
   */
  public List<Patient> searchPatients(String query) {
    // trim input
    query = query.trim();
    if (query.isBlank()) {
      return List.of();
    }

    // search by UUID
    if (queryValidator.isUuid(query)) {
      return patientRepository.findById(UUID.fromString(query)).map(List::of).orElse(List.of());
    }
    // search by phone number
    if (queryValidator.isPhoneNumber(query)) {
      return patientRepository.findByPhoneNumber(query).map(List::of).orElse(List.of());
    }
    // search by single name (first name or last name)
    if (queryValidator.isName(query)) {
      List<Patient> firstNameMatches = patientRepository.findByFirstNameIgnoreCase(query);
      List<Patient> lastNameMatches = patientRepository.findByLastNameIgnoreCase(query);
      firstNameMatches.addAll(lastNameMatches); // Combine results
      return firstNameMatches;
    }
    // search by full name
    if (queryValidator.isFullName(query)) {
      String[] names = query.split(QueryValidator.FULL_NAME_REGEX, 2);
      return patientRepository.findByFullNameIgnoreCase(names[0], names[1]);
    }
    return List.of();
  }

  /**
   * Returns a Patient or throws a ResponseStatusException.
   *
   * @param patientId Id of the User
   */
  public Patient getPatient(UUID patientId) {
    return serviceUtils.findEntityById(patientId, "patient", patientRepository);
  }

  public boolean hasPharmaId(UUID patientId) {
    return getPatient(patientId).getPharmaId() != null;
  }
}