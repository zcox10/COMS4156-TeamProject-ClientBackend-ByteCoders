package com.bytecoders.emergencyaid.service;

import com.bytecoders.emergencyaid.openapi.model.LoginUserRequest;
import com.bytecoders.emergencyaid.openapi.model.LoginUserResponse;
import com.bytecoders.emergencyaid.openapi.model.RegisterUserRequest;
import com.bytecoders.emergencyaid.repository.UserRepository;
import com.bytecoders.emergencyaid.repository.model.User;
import com.bytecoders.emergencyaid.util.JwtUtils;
import com.bytecoders.emergencyaid.util.PasswordUtils;
import com.bytecoders.emergencyaid.util.ServiceUtils;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service operations around {@link User}.
 */
@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordUtils passwordUtils;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private ServiceUtils serviceUtils;

  /**
   * Register new user service.
   *
   * @param registerUserRequest request
   * @return User the newly created user
   */
  public User registerUser(RegisterUserRequest registerUserRequest) {
    final User newUser = new User();
    newUser.setEmail(registerUserRequest.getEmail());
    newUser.setHashedPassword(passwordUtils.hashPassword(registerUserRequest.getPassword()));
    return userRepository.save(newUser);
  }

  /**
   * Login user service.
   *
   * @param loginUserRequest request
   * @return the authenticated user
   */
  public Optional<LoginUserResponse> loginUser(LoginUserRequest loginUserRequest) {
    Optional<User> userWithEmail = userRepository.findByEmail(loginUserRequest.getEmail());

    if (userWithEmail.isEmpty()) {
      return Optional.empty();
    }

    final boolean isCorrectPassword = passwordUtils.verifyPassword(loginUserRequest.getPassword(),
        userWithEmail.get().getHashedPassword());

    if (isCorrectPassword) {
      String token = jwtUtils.generateToken(userWithEmail.get().getId().toString());
      User user = userWithEmail.get();

      LoginUserResponse loginResponse = new LoginUserResponse();
      loginResponse.setId(user.getId());
      loginResponse.setEmail(user.getEmail());
      loginResponse.setToken(token);

      return Optional.of(loginResponse);
    }
    return Optional.empty();
  }

  /**
   * Returns a User or throws a ResponseStatusException.
   *
   * @param userId Id of the User
   */
  public User getUser(UUID userId) {
    return serviceUtils.findEntityById(userId, "user", userRepository);
  }
}