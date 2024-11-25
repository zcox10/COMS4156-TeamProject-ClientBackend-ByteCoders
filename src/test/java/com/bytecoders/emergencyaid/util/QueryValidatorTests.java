package com.bytecoders.emergencyaid.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link QueryValidator}. */
public class QueryValidatorTests {

  private QueryValidator queryValidator;

  @BeforeEach
  void setup() {
    queryValidator = new QueryValidator();
  }

  @Test
  void isUuid_Valid() {
    List<String> inputs = List.of("9101d183-26e6-45b7-a8c4-25f24fdb36fa",
        "   9101d183-26e6-45b7-a8c4-25f24fdb36fa   ");

    for (String input : inputs) {
      // input is always trimmed beforehand
      assertTrue(queryValidator.isUuid(input.trim()),
          String.format("isUuid() should return true for %s", input));
    }
  }

  @Test
  void isUuid_Invalid() {
    List<String> inputs =
        List.of("12345", "invalid-uuid", "9101d183-26e6-45b7-a8c4-25f24fdb36fa-extra", "", "   ",
            "\t");

    for (String input : inputs) {
      assertFalse(queryValidator.isUuid(input.trim()),
          String.format("isUuid() should return false for %s", input));
    }
  }

  @Test
  void isName_Valid() {
    List<String> inputs =
        List.of("John", "Doe", "John-Doe", "John123", "  John ", "\tJohn ", "John-Doe \t",
            "\tJohn\t");

    for (String input : inputs) {
      assertTrue(queryValidator.isName(input.trim()),
          String.format("isName() should return true for %s", input));
    }
  }

  @Test
  void isName_Invalid() {
    List<String> inputs = List.of("John Doe", "\t ", " ", "\t  \t", "");

    for (String input : inputs) {
      assertFalse(queryValidator.isName(input.trim()),
          String.format("isName() should return false for %s", input));
    }
  }

  @Test
  void isFullName_Valid() {
    List<String> inputs =
        List.of("Jane Doe", "Jane   Doe", "Jane\tDoe", "  Jane\t Doe ", "Jane O'Doe",
            "Jane Mary-Doe", "Jane Mary-Doe");

    for (String input : inputs) {
      assertTrue(queryValidator.isFullName(input.trim()),
          String.format("isFullName() should return true for %s", input));
    }
  }

  @Test
  void isFullName_Invalid() {
    List<String> inputs =
        List.of("Jane", "Jane ", " Jane  ", "  Jane \t", "\tJane\t", "\tJane ", "Jane\t", "",
            "  ", "  \t", "\t  ", "Jane-Doe");

    for (String input : inputs) {
      assertFalse(queryValidator.isFullName(input.trim()),
          String.format("isFullName() should return false for %s", input));
    }
  }

  @Test
  void isPhoneNumber_Valid() {
    List<String> inputs =
        List.of("123-456-7890", "800-555-1234", "  999-999-9999  ", "\t000-000-0000 ");

    for (String input : inputs) {
      assertTrue(queryValidator.isPhoneNumber(input.trim()),
          String.format("isPhoneNumber() should return true for %s", input));
    }
  }

  @Test
  void isPhoneNumber_Invalid() {
    List<String> inputs =
        List.of("1234567890", "123-45-6789", "12-3456-7890", "123-4567-890", "123-456-789",
            "123-456-78901", "abc-def-ghij", "123-456-78ab", "   ", "");

    for (String input : inputs) {
      assertFalse(queryValidator.isPhoneNumber(input.trim()),
          "isPhoneNumber() should return false for null input");
    }
  }
}
