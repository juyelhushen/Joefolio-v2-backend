package com.juyel.joefolio_backend.contact.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

class ContactRequestTest {

  @Test
  void shouldAcceptValidContactRequest() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {

      Validator validator = factory.getValidator();

      ContactRequest request =
          new ContactRequest(
              "Joe",
              "joe@example.com",
              "I would like to discuss an opportunity.",
              "body",
              "https://example.com");

      assertTrue(validator.validate(request).isEmpty());
    }
  }

  @Test
  void shouldRejectBlankName() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {

      Validator validator = factory.getValidator();

      ContactRequest request =
          new ContactRequest("   ", "joe@example.com", "Hello", "body", "https://example.com");

      assertFalse(validator.validate(request).isEmpty());
    }
  }

  @Test
  void shouldRejectInvalidEmail() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {

      Validator validator = factory.getValidator();

      ContactRequest request =
          new ContactRequest("Joe", "not-an-email", "Hello", "body", "https://example.com");

      assertFalse(validator.validate(request).isEmpty());
    }
  }

  @Test
  void shouldRejectBlankSubject() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {

      Validator validator = factory.getValidator();

      ContactRequest request =
          new ContactRequest("Joe", "joe@example.com", "   ", "body", "https://example.com");

      assertFalse(validator.validate(request).isEmpty());
    }
  }

  @Test
  void shouldRejectBlankMessage() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {

      Validator validator = factory.getValidator();

      ContactRequest request =
          new ContactRequest("Joe", "joe@example.com", "Hello", "   ", "https://example.com");

      assertFalse(validator.validate(request).isEmpty());
    }
  }

  @Test
  void shouldRejectMessageExceedingMaximumLength() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {

      Validator validator = factory.getValidator();

      ContactRequest request =
          new ContactRequest(
              "Joe", "joe@example.com", "Hello", "a".repeat(5001), "https://example.com");

      assertFalse(validator.validate(request).isEmpty());
    }
  }

  @Test
  void shouldRejectWebsiteExceedingMaximumLength() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {

      Validator validator = factory.getValidator();

      ContactRequest request =
          new ContactRequest(
              "Joe", "joe@example.com", "Hello", "body", "https://example.com/" + "a".repeat(200));

      assertFalse(validator.validate(request).isEmpty());
    }
  }
}
