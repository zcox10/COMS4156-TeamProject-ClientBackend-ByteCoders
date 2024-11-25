package com.bytecoders.emergencyaid;

import com.bytecoders.emergencyaid.security.JwtRequestFilter;
import com.bytecoders.emergencyaid.util.JwtUtils;
import com.bytecoders.emergencyaid.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

/** Config class for EmergencyAid security and password management settings. */
@Configuration
@EnableWebSecurity
public class AppConfig {

  @Autowired
  private JwtRequestFilter jwtRequestFilter;

  @Autowired
  private JwtUtils jwtUtils;

  @Bean
  public PasswordUtils passwordUtils() {
    return new PasswordUtils();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /**
   * Configure the security filter chain. Defines the endpoints that can be accessed without auth
   * via requestMatchers().
   *
   * @param http the HttpSecurity to configure
   * @return the configured SecurityFilterChain
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> {
      auth.requestMatchers(jwtUtils.getPublicEndpoints()).permitAll();
      auth.anyRequest().authenticated();
    }).sessionManagement(
        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}