package com.bytecoders.emergencyaid.service;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/** Auth Service to login to PharmaId. */
@Slf4j
@Service
public class PharmaidAuthService {

  private final RestTemplate restTemplate;
  private final String pharmaidBaseUrl;
  private final String pharmaidClientEmail;
  private final String pharmaidClientPass;

  @Getter
  private String authToken;

  public PharmaidAuthService(
      RestTemplate restTemplate,
      @Value("${pharmaid.api.base-url}") String pharmaidBaseUrl,
      @Value("${pharmaid.api.email}") String pharmaidClientEmail,
      @Value("${pharmaid.api.password}") String pharmaidClientPass) {
    this.restTemplate = restTemplate;
    this.pharmaidBaseUrl = pharmaidBaseUrl;
    this.pharmaidClientEmail = pharmaidClientEmail;
    this.pharmaidClientPass = pharmaidClientPass;
  }

  /** Login request to the PharmaId API. */
  public void login() {
    try {
      // create login request
      String pharmaidLoginEndpoint = pharmaidBaseUrl + "/login";

      Map<String, String> loginRequest = createLoginRequest();

      ResponseEntity<Map> response =
          restTemplate.postForEntity(pharmaidLoginEndpoint, loginRequest, Map.class);

      checkLoginStatus(response);
    } catch (Exception e) {
      log.error("Error during PharmaId login", e);
      throw new IllegalStateException("Error during PharmaId login", e);
    }
  }

  /**
   * Creates a login request body.
   *
   * @return a request body containing login email and password
   */
  public Map<String, String> createLoginRequest() {
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("email", pharmaidClientEmail);
    loginRequest.put("password", pharmaidClientPass);
    return loginRequest;
  }

  /**
   * Checks the login status to ensure a successful login to PharmaId API.
   *
   * @throws IllegalStateException if login fails
   */
  public void checkLoginStatus(ResponseEntity<Map> response) {
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      authToken = (String) response.getBody().get("token");
      log.info("PharmaId login successful.");
    } else {
      throw new IllegalStateException("Failed to login to PharmaId: " + response.getStatusCode());
    }
  }

  /**
   * Constructs and returns the HTTP headers required PharmaId API.
   *
   * @return {@link HttpHeaders} containing auth and Content-Type headers
   * @throws IllegalStateException if the auth token is null or not set.
   */
  public HttpHeaders getHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + authToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
