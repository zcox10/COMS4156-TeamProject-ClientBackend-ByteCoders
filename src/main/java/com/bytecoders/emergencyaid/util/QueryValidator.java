package com.bytecoders.emergencyaid.util;

import java.util.UUID;
import org.springframework.stereotype.Component;

/** Utils for validating query inputs across various classes. */
@Component
public class QueryValidator {

  // Regex for phone numbers. Example: 800-100-1000; 3 digits, dash, 3 digits, dash, 4 digits
  public static final String PHONE_NUMBER_REGEX = "\\d{3}-\\d{3}-\\d{4}";

  // Regex for splitting full names. Must include a space, then split on the space for full name
  public static final String FULL_NAME_REGEX = "\\s+";

  /**
   * Determines if an input query is a valid UUID.
   *
   * @param query the search query
   * @return true if valid UUID, else false
   */
  public boolean isUuid(String query) {
    try {
      UUID.fromString(query);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public boolean isPhoneNumber(String query) {
    return query.matches(PHONE_NUMBER_REGEX);
  }

  public boolean isName(String query) {
    return !query.isBlank() && !query.contains(" ");
  }

  public boolean isFullName(String query) {
    return query.split(FULL_NAME_REGEX, 2).length == 2;
  }

  public String safeTrim(String input) {
    return input != null ? input.trim() : null;
  }
}
