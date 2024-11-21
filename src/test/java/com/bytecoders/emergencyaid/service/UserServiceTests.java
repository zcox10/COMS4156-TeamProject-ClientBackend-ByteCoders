package com.bytecoders.emergencyaid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.bytecoders.emergencyaid.openapi.model.LoginUserRequest;
import com.bytecoders.emergencyaid.openapi.model.LoginUserResponse;
import com.bytecoders.emergencyaid.openapi.model.RegisterUserRequest;
import com.bytecoders.emergencyaid.repository.UserRepository;
import com.bytecoders.emergencyaid.repository.model.User;
import com.bytecoders.emergencyaid.util.JwtUtils;
import com.bytecoders.emergencyaid.util.PasswordUtils;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link UserService}.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

  @Mock
  private JwtUtils jwtUtils;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordUtils passwordUtils;

  @InjectMocks
  private UserService userService = new UserService();

  private static final UUID MOCK_UUID = UUID.fromString("9101d183-26e6-45b7-a8c4-25f24fdb36fa");

  @Test
  public void testRegisterUser() {
    final RegisterUserRequest request = new RegisterUserRequest();
    request.setEmail("email@test.com");
    request.setPassword("password");

    final User expectedUser = new User();
    expectedUser.setId(MOCK_UUID);
    expectedUser.setEmail("email@test.com");
    expectedUser.setHashedPassword("hashedPassword");

    doAnswer((Answer<User>) invocation -> {
      User user = invocation.getArgument(0);
      user.setId(MOCK_UUID);
      return user;
    }).when(userRepository).save(any(User.class));
    when(passwordUtils.hashPassword("password")).thenReturn("hashedPassword");

    final User actualUser = userService.registerUser(request);
    assertEquals(actualUser, expectedUser);
  }

  @Test
  public void testLoginSuccess() {
    final LoginUserRequest request = new LoginUserRequest();
    request.setEmail("email@test.com");
    request.setPassword("password");

    final User mockUser = new User();
    mockUser.setId(MOCK_UUID);
    mockUser.setEmail("email@test.com");
    mockUser.setHashedPassword("hashedPassword");

    LoginUserResponse mockLoginResponse = new LoginUserResponse();
    mockLoginResponse.setId(mockUser.getId());
    mockLoginResponse.setEmail(mockUser.getEmail());
    mockLoginResponse.setToken("mockJwtToken");

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
    when(passwordUtils.verifyPassword("password", "hashedPassword")).thenReturn(true);
    when(jwtUtils.generateToken(mockUser.getId().toString())).thenReturn("mockJwtToken");

    final Optional<LoginUserResponse> loginResponseOptional = userService.loginUser(request);
    assertEquals(loginResponseOptional, Optional.of(mockLoginResponse));
  }

  @Test
  public void testLoginNoSuchUser() {
    final LoginUserRequest request = new LoginUserRequest();
    request.setEmail("email@test.com");
    request.setPassword("password");

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

    final Optional<LoginUserResponse> loginResponseOptional = userService.loginUser(request);
    assertEquals(loginResponseOptional, Optional.empty());
  }

  @Test
  public void testLoginInvalidPassword() {
    final LoginUserRequest request = new LoginUserRequest();
    request.setEmail("email@test.com");
    request.setPassword("password");

    final User mockUser = new User();
    mockUser.setId(MOCK_UUID);
    mockUser.setEmail("email@test.com");
    mockUser.setHashedPassword("hashedPassword");

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
    when(passwordUtils.verifyPassword("password", "hashedPassword")).thenReturn(false);

    final Optional<LoginUserResponse> loginResponseOptional = userService.loginUser(request);
    assertEquals(loginResponseOptional, Optional.empty());
  }
}
