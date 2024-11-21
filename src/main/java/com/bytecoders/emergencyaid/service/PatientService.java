package com.bytecoders.emergencyaid.service;

import com.bytecoders.emergencyaid.openapi.model.RegisterPatientRequest;
import com.bytecoders.emergencyaid.repository.PatientRepository;
import com.bytecoders.emergencyaid.repository.model.Patient;
import com.bytecoders.emergencyaid.util.ServiceUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.RestTemplate;

/**
 * Service operations around {@link Patient}.
 */
@Service
public class PatientService {

  @Autowired
  private PatientRepository patientRepository;

  @Autowired
  private ServiceUtils serviceUtils;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${pharmaid.api.base-url}")
  private String pharmaidBaseUrl;

  /**
   * Register new patient service.
   *
   * @param registerPatientRequest request
   * @return Patient the newly created patient
   */
  public Patient registerPatient(RegisterPatientRequest registerPatientRequest) {
    final Patient newPatient = new Patient();
    newPatient.setFirstName(registerPatientRequest.getFirstName());
    newPatient.setLastName(registerPatientRequest.getLastName());
    newPatient.setPhoneNumber(registerPatientRequest.getPhoneNumber());
    newPatient.setPharmaId(registerPatientRequest.getPharmaId());
    return patientRepository.save(newPatient);
  }

  /**
   * Retrieve patient prescriptions from PharmaId.
   *
   * @param pharmaId the patient's pharmaId
   * @return a list of patient's prescriptions
   */
  public List<Map<String, Object>> getPatientPrescriptions(String pharmaId) {
    if (hasPharmaId(pharmaId)) {
      return Collections.emptyList();
    }
    try {
      // Return raw response as a list of prescriptions
      String url = String.format("%s/users/%s/prescriptions", pharmaidBaseUrl, pharmaId);
      return restTemplate.exchange(url, HttpMethod.GET, null,
          new ParameterizedTypeReference<List<Map<String, Object>>>() {
          }).getBody();
    } catch (NotFound e) {
      // No prescriptions found for patient
      return Collections.emptyList();
    } catch (Exception e) {
      throw new IllegalStateException("Error while retrieving prescriptions from PharmaId", e);
    }
  }

  /**
   * Provide a query to search for patients by UUID, phone number, or full name.
   *
   * @param query input a UUID, phone number, or patient full name
   * @return list of patients matching the query criteria
   */
  public List<Patient> searchPatients(String query) {
    // search by UUID
    if (isUuid(query)) {
      return patientRepository.findById(UUID.fromString(query)).map(List::of).orElse(List.of());
    }
    // search by phone number
    if (isPhoneNumber(query)) {
      return patientRepository.findByPhoneNumber(query).map(List::of).orElse(List.of());
    }
    // search by full name
    if (isFullName(query)) {
      String[] names = query.split(ServiceUtils.FULL_NAME_REGEX, 2);
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

  private boolean isUuid(String query) {
    try {
      UUID.fromString(query);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private boolean isPhoneNumber(String query) {
    return query.matches(ServiceUtils.PHONE_NUMBER_REGEX);
  }

  private boolean isFullName(String query) {
    return query.split(ServiceUtils.FULL_NAME_REGEX, 2).length == 2;
  }

  public boolean hasPharmaId(String patientPharmaId) {
    return patientPharmaId != null;
  }
}