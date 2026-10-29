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
              "Joe", "joe@example.com", "I would like to discuss an opportunity.", "body");

      assertTrue(validator.validate(request).isEmpty());
    }
  }

  @Test
  void shouldRejectBlankName() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {

      Validator validator = factory.getValidator();

      ContactRequest request = new ContactRequest("   ", "joe@example.com", "Hello", "body");

      assertFalse(validator.validate(request).isEmpty());
    }
  }

  @Test
  void shouldRejectInvalidEmail() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {

      Validator validator = factory.getValidator();

      ContactRequest request = new ContactRequest("Joe", "not-an-email", "Hello", "body");

      assertFalse(validator.validate(request).isEmpty());
    }
  }

  @Test
  void shouldRejectBlankMessage() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {

      Validator validator = factory.getValidator();

      ContactRequest request = new ContactRequest("Joe", "joe@example.com", "   ", "body");

      assertFalse(validator.validate(request).isEmpty());
    }
  }

  @Test
  void shouldRejectMessageExceedingMaximumLength() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {

      Validator validator = factory.getValidator();

      ContactRequest request =
          new ContactRequest("Joe", "joe@example.com", "a".repeat(2001), "body");

      assertFalse(validator.validate(request).isEmpty());
    }
  }
}
