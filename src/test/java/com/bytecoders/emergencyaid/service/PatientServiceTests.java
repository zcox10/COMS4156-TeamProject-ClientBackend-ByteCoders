package com.bytecoders.emergencyaid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.bytecoders.emergencyaid.openapi.model.RegisterPatientRequest;
import com.bytecoders.emergencyaid.repository.PatientRepository;
import com.bytecoders.emergencyaid.repository.model.Patient;
import com.bytecoders.emergencyaid.util.QueryValidator;
import com.bytecoders.emergencyaid.util.ServiceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

/** Tests for {@link PatientService}. */
@ExtendWith(MockitoExtension.class)
public class PatientServiceTests {

  @Value("${pharmaid.api.base-url}")
  private String pharmaidBaseUrl;

  @Value("${pharmaid.api.pharmaid}")
  private String clientPharmaId;

  @Mock
  private PatientRepository patientRepository;

  @Mock
  private ServiceUtils serviceUtils;

  @Mock
  private PharmaidAuthService pharmaidAuthService;

  @Mock
  private QueryValidator queryValidator;

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private PatientService patientService;

  private static final UUID MOCK_UUID = UUID.randomUUID();
  private static final String MOCK_PHARMAID = UUID.randomUUID().toString();
  private Patient patient;
  private List<Map<String, Object>> prescriptions;
  HttpHeaders headers;

  @BeforeEach
  void setup() {
    patient = new Patient();
    patient.setId(MOCK_UUID);
    patient.setFirstName("John");
    patient.setFirstName("Doe");
    patient.setPhoneNumber("800-100-9999");

    prescriptions = List.of(Map.of("id", "8e5d0073-bee1-48d9-a85d-a39ce230caa2", "medication",
        Map.of("id", "7f72c869-7f99-4a77-8242-598a6b933ded", "medicationName", "Ibuprofen"),
        "dosage", 205, "numOfDoses", 32, "startDate", "2024-10-28", "endDate", "2024-11-28",
        "isActive", true), Map.of("id", "9f1c0074-cdd1-49c9-b75c-b49ce120caa3", "medication",
        Map.of("id", "6e82b869-8f99-4b88-9242-498a5b933def", "medicationName", "Paracetamol"),
        "dosage", 500, "numOfDoses", 60, "startDate", "2024-10-01", "endDate", "2024-11-01",
        "isActive", false));

    // http headers
    headers = new HttpHeaders();
    headers.set("Authorization", "Bearer mockToken");
    headers.setContentType(MediaType.APPLICATION_JSON);
  }

  @Test
  void getPatient_PatientExists() {
    when(serviceUtils.findEntityById(MOCK_UUID, "patient", patientRepository)).thenReturn(
        patient);
    Patient result = patientService.getPatient(MOCK_UUID);
    assertEquals(patient, result, "getPatient() should return the correct patient object");
  }

  @Test
  void getPatient_PatientDoesNotExist() {
    // patient does not exist -> throw exception
    when(serviceUtils.findEntityById(MOCK_UUID, "patient", patientRepository)).thenThrow(
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Provided patientId does not exist"));

    // throw exception
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> patientService.getPatient(MOCK_UUID));

    // assertions
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(), "Should throw 404 NOT FOUND");
    assertEquals("Provided patientId does not exist", exception.getReason(),
        "Exception message should match the expected reason");
  }

  @Test
  void hasPharmaId_True_PharmaIdExists() {
    // set a valid PharmaId; mock getPatient()
    patient.setPharmaId(MOCK_PHARMAID);
    when(patientService.getPatient(MOCK_UUID)).thenReturn(patient);

    boolean result = patientService.hasPharmaId(MOCK_UUID);
    assertTrue(result, "hasPharmaId() should return true when PharmaId exists");
  }

  @Test
  void hasPharmaId_False_PharmaIdDoesNotExist() {
    // mock getPatient() without pharmaId
    when(patientService.getPatient(MOCK_UUID)).thenReturn(patient);

    boolean result = patientService.hasPharmaId(MOCK_UUID);
    assertFalse(result, "hasPharmaId() should return false when PharmaId does not exist");
  }

  @Test
  void registerPatient_ValidRequest() {
    // create RegisterPatientRequest
    RegisterPatientRequest request = new RegisterPatientRequest();
    request.setFirstName(patient.getFirstName());
    request.setLastName(patient.getLastName());
    request.setPhoneNumber(patient.getPhoneNumber());
    request.setPharmaId(MOCK_PHARMAID);

    // set values for mockSavedPatient
    Patient mockSavedPatient = new Patient();
    mockSavedPatient.setId(UUID.randomUUID());
    mockSavedPatient.setFirstName(patient.getFirstName());
    mockSavedPatient.setLastName(patient.getLastName());
    mockSavedPatient.setPhoneNumber(patient.getPhoneNumber());
    mockSavedPatient.setPharmaId(MOCK_PHARMAID);

    // mock validations
    when(queryValidator.safeTrim(patient.getFirstName())).thenReturn(patient.getFirstName());
    when(queryValidator.safeTrim(patient.getLastName())).thenReturn(patient.getLastName());
    when(queryValidator.safeTrim(patient.getPhoneNumber())).thenReturn(patient.getPhoneNumber());
    when(queryValidator.safeTrim(MOCK_PHARMAID)).thenReturn(MOCK_PHARMAID);

    when(queryValidator.isPhoneNumber(patient.getPhoneNumber())).thenReturn(true);
    when(queryValidator.isName(patient.getFirstName())).thenReturn(true);
    when(queryValidator.isName(patient.getLastName())).thenReturn(true);

    // mock save
    when(patientRepository.save(any(Patient.class))).thenReturn(mockSavedPatient);
    Patient savedPatient = patientService.registerPatient(request);

    // assertions
    assertEquals(patient.getFirstName(), savedPatient.getFirstName());
    assertEquals(patient.getLastName(), savedPatient.getLastName());
    assertEquals(patient.getPhoneNumber(), savedPatient.getPhoneNumber());
    assertEquals(MOCK_PHARMAID, savedPatient.getPharmaId());
  }

  @Test
  void registerPatient_ValidRequestWithSpaces() {
    final String firstName = "  " + patient.getFirstName() + "  ";
    final String lastName = "  " + patient.getLastName() + "  ";
    final String phoneNumber = "  " + patient.getPhoneNumber() + "  ";

    // create RegisterPatientRequest
    RegisterPatientRequest request = new RegisterPatientRequest();
    request.setFirstName(firstName);
    request.setLastName(lastName);
    request.setPhoneNumber(phoneNumber);
    request.setPharmaId(MOCK_PHARMAID);

    // set values for mockSavedPatient
    Patient mockSavedPatient = new Patient();
    mockSavedPatient.setId(UUID.randomUUID());
    mockSavedPatient.setFirstName(patient.getFirstName());
    mockSavedPatient.setLastName(patient.getLastName());
    mockSavedPatient.setPhoneNumber(patient.getPhoneNumber());
    mockSavedPatient.setPharmaId(MOCK_PHARMAID);

    // mock validations
    when(queryValidator.safeTrim(firstName)).thenReturn(patient.getFirstName());
    when(queryValidator.safeTrim(lastName)).thenReturn(patient.getLastName());
    when(queryValidator.safeTrim(phoneNumber)).thenReturn(patient.getPhoneNumber());
    when(queryValidator.safeTrim(MOCK_PHARMAID)).thenReturn(MOCK_PHARMAID);

    when(queryValidator.isPhoneNumber(patient.getPhoneNumber())).thenReturn(true);
    when(queryValidator.isName(patient.getFirstName())).thenReturn(true);
    when(queryValidator.isName(patient.getLastName())).thenReturn(true);

    // mock save
    when(patientRepository.save(any(Patient.class))).thenReturn(mockSavedPatient);
    Patient savedPatient = patientService.registerPatient(request);

    // assertions
    assertEquals(patient.getFirstName(), savedPatient.getFirstName());
    assertEquals(patient.getLastName(), savedPatient.getLastName());
    assertEquals(patient.getPhoneNumber(), savedPatient.getPhoneNumber());
    assertEquals(MOCK_PHARMAID, savedPatient.getPharmaId());
  }

  @Test
  void registerPatient_InvalidRequest_InvalidPhoneNumber() {
    // create RegisterPatientRequest with invalid phone number (no dashes)
    RegisterPatientRequest request = new RegisterPatientRequest();
    request.setFirstName(patient.getFirstName());
    request.setLastName(patient.getLastName());
    request.setPhoneNumber("1234567890");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      patientService.registerPatient(request);
    });

    assertEquals("Phone number must be 10 digits with dash separators", exception.getMessage());
  }

  @Test
  void requestPatientPrescriptionAccess_SuccessfulRequest() {
    // mock PharmaIdAuthService to return headers
    when(pharmaidAuthService.getHeaders()).thenReturn(headers);

    // Construct expected URL and request body
    String url = String.format("%s/users/%s/requests", pharmaidBaseUrl, MOCK_PHARMAID);

    // Mock RestTemplate to return a successful response
    when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(Object.class))).thenReturn(
        new ResponseEntity<>(HttpStatus.CREATED));

    // Call the method under test
    boolean result = patientService.requestPatientPrescriptionAccess(MOCK_PHARMAID);

    // Assertions
    assertTrue(result,
        "requestPatientPrescriptionAccess() should return true for a successful request");
  }

  @Test
  void requestPatientPrescriptionAccess_FailedRequest() {
    // mock PharmaIdAuthService to return headers
    when(pharmaidAuthService.getHeaders()).thenReturn(headers);

    // create ShareRequest request
    String url = String.format("%s/users/%s/requests", pharmaidBaseUrl, MOCK_PHARMAID);

    // mock a non-CREATED HttpStatus
    when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(Object.class))).thenReturn(
        new ResponseEntity<>(HttpStatus.BAD_REQUEST));

    boolean result = patientService.requestPatientPrescriptionAccess(MOCK_PHARMAID);
    assertFalse(result,
        "requestPatientPrescriptionAccess() should return false for a failed request");
  }

  @Test
  void requestPatientPrescriptionAccess_ExceptionThrown() {
    // mock PharmaIdAuthService to return headers
    when(pharmaidAuthService.getHeaders()).thenReturn(headers);

    // create ShareRequest request
    String url = String.format("%s/users/%s/requests", pharmaidBaseUrl, MOCK_PHARMAID);

    // mock a RuntimeException
    when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(Object.class))).thenThrow(
        new RuntimeException("Connection error"));

    // throw exception
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
      patientService.requestPatientPrescriptionAccess(MOCK_PHARMAID);
    });
    assertEquals("Error while requesting access to prescriptions: Connection error",
        exception.getMessage());
    assertNotNull(exception.getCause(), "Cause of the exception should not be null");
    assertEquals("Connection error", exception.getCause().getMessage(),
        "Cause message should match the thrown exception");
  }

  @Test
  void getPatientPrescriptions_Success() {
    // mock PharmaIdAuthService to return headers
    when(pharmaidAuthService.getHeaders()).thenReturn(headers);

    final String getPrescriptionsEndpoint =
        String.format("%s/users/%s/prescriptions", pharmaidBaseUrl, MOCK_PHARMAID);
    final String shareRequestEndpoint =
        String.format("%s/users/%s/requests", pharmaidBaseUrl, MOCK_PHARMAID);

    patient.setPharmaId(MOCK_PHARMAID);
    when(patientService.getPatient(patient.getId())).thenReturn(patient);

    // mock ShareRequest access
    when(restTemplate.postForEntity(eq(shareRequestEndpoint), any(HttpEntity.class),
        eq(Object.class))).thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

    // mock getPrescriptions
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    when(restTemplate.exchange(eq(getPrescriptionsEndpoint), eq(HttpMethod.GET), eq(entity),
        any(ParameterizedTypeReference.class))).thenReturn(
        new ResponseEntity<>(prescriptions, HttpStatus.OK));

    // getPatientPrescriptions
    List<Map<String, Object>> mockPrescriptions =
        patientService.getPatientPrescriptions(patient.getId());

    assertEquals(2, prescriptions.size(), "Prescriptions should contain two items");
    assertEquals(prescriptions.get(0).get("id"), mockPrescriptions.get(0).get("id"));
    assertEquals(prescriptions.get(1).get("id"), mockPrescriptions.get(1).get("id"));
  }

  @Test
  void getPatientPrescriptions_EmptyList_NoPharmaId() {
    // mock patient with no PharmaId
    when(serviceUtils.findEntityById(patient.getId(), "patient", patientRepository)).thenReturn(
        patient);

    // act
    List<Map<String, Object>> prescriptions =
        patientService.getPatientPrescriptions(patient.getId());

    // assert
    assertTrue(prescriptions.isEmpty(), "Prescriptions should be empty when no PharmaId exists");
  }

  @Test
  void searchPatients_Blank_ReturnEmptyList() {
    List<Patient> result = patientService.searchPatients("   ");
    assertTrue(result.isEmpty(), "Result should be empty for a blank query");
  }

  @Test
  void searchPatients_ValidUuid_ReturnsPatient() {
    String query = MOCK_UUID.toString();

    // mock isUuid() and findById()
    when(queryValidator.isUuid(query)).thenReturn(true);
    when(patientRepository.findById(MOCK_UUID)).thenReturn(Optional.of(patient));

    List<Patient> result = patientService.searchPatients(query);

    // assertions
    assertEquals(1, result.size(), "Result should contain one patient for a valid UUID");
    assertEquals(patient, result.get(0), "The returned patient should match the mock patient");
  }

  @Test
  void searchPatients_ValidPhoneNumber_ReturnsPatient() {
    // mock isUuid() and findById()
    when(queryValidator.isUuid(patient.getPhoneNumber())).thenReturn(false);
    when(queryValidator.isPhoneNumber(patient.getPhoneNumber())).thenReturn(true);
    when(patientRepository.findByPhoneNumber(patient.getPhoneNumber())).thenReturn(
        Optional.of(patient));

    List<Patient> result = patientService.searchPatients(patient.getPhoneNumber());

    // assertions
    assertEquals(1, result.size(), "Result should contain one patient for a valid UUID");
    assertEquals(patient, result.get(0), "The returned patient should match the mock patient");
  }

  @Test
  void searchPatients_ValidFirstName_ReturnsPatients() {
    // create patient1 and patient2
    String query = "John";

    Patient patient1 = new Patient();
    patient1.setFirstName(query);

    Patient patient2 = new Patient();
    patient2.setFirstName(query);

    List<Patient> mockPatients = new ArrayList<>(List.of(patient1, patient2));

    // mock validations
    when(queryValidator.isUuid(query)).thenReturn(false);
    when(queryValidator.isPhoneNumber(query)).thenReturn(false);

    when(queryValidator.isName(query)).thenReturn(true);
    when(patientRepository.findByFirstNameIgnoreCase(query)).thenReturn(mockPatients);
    when(patientRepository.findByLastNameIgnoreCase(query)).thenReturn(List.of());

    List<Patient> result = patientService.searchPatients(query);

    assertEquals(2, result.size(),
        String.format("Result should contain all patients with the first name '%s'", query));
    assertTrue(result.contains(patient1), "Result should contain patient1");
    assertTrue(result.contains(patient2), "Result should contain patient2");
  }

  @Test
  void searchPatients_ValidFullName_ReturnsPatients() {
    // create name; split on first and last name
    final String query = "John Doe";
    final String[] names = query.split(QueryValidator.FULL_NAME_REGEX, 2);

    // create patient1 and patient2
    Patient patient1 = new Patient();
    patient1.setFirstName(query);

    Patient patient2 = new Patient();
    patient2.setFirstName(query);

    List<Patient> mockPatients = new ArrayList<>(List.of(patient1, patient2));

    // mock validations
    when(queryValidator.isUuid(query)).thenReturn(false);
    when(queryValidator.isPhoneNumber(query)).thenReturn(false);
    when(queryValidator.isName(query)).thenReturn(false);

    when(queryValidator.isFullName(query)).thenReturn(true);
    when(patientRepository.findByFullNameIgnoreCase(names[0], names[1])).thenReturn(mockPatients);

    List<Patient> result = patientService.searchPatients(query);

    assertEquals(2, result.size(),
        String.format("Result should contain all patients with the full name '%s'", query));
    assertTrue(result.contains(patient1), "Result should contain patient1");
    assertTrue(result.contains(patient2), "Result should contain patient2");
  }


}
