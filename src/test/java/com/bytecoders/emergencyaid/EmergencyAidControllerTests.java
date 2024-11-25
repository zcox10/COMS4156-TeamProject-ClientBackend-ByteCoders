package com.bytecoders.emergencyaid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.bytecoders.emergencyaid.openapi.model.LoginUserRequest;
import com.bytecoders.emergencyaid.openapi.model.LoginUserResponse;
import com.bytecoders.emergencyaid.openapi.model.RegisterUserRequest;
import com.bytecoders.emergencyaid.repository.model.User;
import com.bytecoders.emergencyaid.service.PatientService;
import com.bytecoders.emergencyaid.service.UserService;
import com.bytecoders.emergencyaid.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * This class represents a set of unit tests for {@code EmergencyAidController} class.
 */
@WebMvcTest(EmergencyAidController.class)
public class EmergencyAidControllerTests {

  private static final UUID MOCK_UUID = UUID.fromString("9101d183-26e6-45b7-a8c4-25f24fdb36fa");

  @Test
  public void registerSuccessTest() {
    final RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("ol2260@columbia.edu");
    request.setPassword("password");

    final User mockUser = new User();
    mockUser.setId(MOCK_UUID);
    mockUser.setEmail("ol2260@columbia.edu");

    when(userService.registerUser(request)).thenReturn(mockUser);

    final ResponseEntity<?> actualUser = testController.registerUser(request);
    assertEquals(actualUser.getStatusCode(), HttpStatus.CREATED);
    assertEquals(actualUser.getBody(), mockUser);
  }

  @Test
  public void registerUserAlreadyExistsTest() {
    final RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("ol2260@columbia.edu");
    request.setPassword("password");

    when(userService.registerUser(request)).thenThrow(DataIntegrityViolationException.class);

    final ResponseEntity<?> actualUser = testController.registerUser(request);
    assertEquals(actualUser.getStatusCode(), HttpStatus.BAD_REQUEST);
    assertEquals(actualUser.getBody(), "User already exists for this email");
  }

  @Test
  public void registerUserUnexpectedErrorTest() {
    final RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("ol2260@columbia.edu");
    request.setPassword("password");

    when(userService.registerUser(request)).thenThrow(RuntimeException.class);

    final ResponseEntity<?> actualUser = testController.registerUser(request);
    assertEquals(actualUser.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    assertEquals(actualUser.getBody(), "Something went wrong");
  }

  /**
   * Test for successful user login.
   */
  @Test
  void testLoginSuccess() throws Exception {
    LoginUserRequest request = new LoginUserRequest();
    request.setEmail("test@example.com");
    request.setPassword("password");

    LoginUserResponse mockLoginResponse = new LoginUserResponse();
    mockLoginResponse.setId(MOCK_UUID);
    mockLoginResponse.setEmail("test@example.com");
    mockLoginResponse.setToken("mock.jwt.token");

    when(userService.loginUser(request)).thenReturn(Optional.of(mockLoginResponse));
    ResponseEntity<?> response = testController.loginUser(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(mockLoginResponse, response.getBody());
  }

  /**
   * Test for failed user login.
   */
  @Test
  void testLoginFailed() {
    LoginUserRequest request = new LoginUserRequest();
    request.setEmail("wrong@example.com");
    request.setPassword("wrongpassword");

    when(userService.loginUser(request)).thenReturn(Optional.empty());

    ResponseEntity<?> response = testController.loginUser(request);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Invalid email or password", response.getBody());
  }

  @Autowired
  public EmergencyAidController testController;

  @MockBean
  private UserService userService;

  @MockBean
  private PatientService patientService;

  @MockBean
  private JwtUtils jwtUtils;

  @Autowired
  private ObjectMapper objectMapper;
}
