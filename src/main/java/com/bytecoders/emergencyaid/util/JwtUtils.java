package com.bytecoders.emergencyaid.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utils to generate, parse, and validate JSON Web Tokens (JWT).
 */
@Component
public class JwtUtils {

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.expiration-time}")
  private long jwtExpiration;

  /**
   * Generate a JWT token with a specified user identifier.
   *
   * @param userId Unique identifier for the user.
   * @return signed JWT as a String, with userId as the subject.
   */
  public String generateToken(String userId) {
    return Jwts.builder().setSubject(userId).setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();
  }

  public String extractUserId(String token) {
    return getClaims(token).getSubject();
  }

  public boolean isTokenValid(String token, String userId) {
    return extractUserId(token).equals(userId) && !isTokenExpired(token);
  }

  /**
   * Define public endpoints that do not require authentication.
   *
   * @return the list of public endpoints
   */
  public String[] getPublicEndpoints() {
    return new String[]{"/hello",
        "/login",
        "/register",
        "/patients/new",
        "/emergency-aid-docs",
        "/emergency-aid-docs/swagger-config",
        "/emergency-aid-docs-ui.html",
        "/swagger-ui/index.html",
        "/swagger-ui/swagger-ui.css",
        "/swagger-ui/swagger-ui-bundle.js",
        "/swagger-ui/swagger-ui-standalone-preset.js",
        "/swagger-ui/swagger-initializer.js"};
  }

  private Claims getClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token)
        .getBody();
  }

  private boolean isTokenExpired(String token) {
    return getClaims(token).getExpiration().before(new Date());
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Fetches logged-in user.
   */
  public String getLoggedInUserId() {
    org.springframework.security.core.Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      return (String) authentication.getPrincipal();
    }
    return null;
  }
}
