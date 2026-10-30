package com.juyel.joefolio_backend.contact.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.juyel.joefolio_backend.contact.service.ContactRateLimiter;
import com.juyel.joefolio_backend.contact.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

public class ContactControllerTest {

  private ContactService contactService;
  private ContactRateLimiter contactRateLimiter;
  private ContactController controller;

  @BeforeEach
  void setUp() {
    contactService = mock(ContactService.class);
    contactRateLimiter = mock(ContactRateLimiter.class);

    controller = new ContactController(contactService, contactRateLimiter);
  }

  @Test
  void acceptsLegitimateContactSubmission() {

    when(contactRateLimiter.isAllowed("203.0.113.10")).thenReturn(true);

    MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    servletRequest.addHeader("CF-Connecting-IP", "203.0.113.10");

    ContactRequest request = createRequest("");

    ResponseEntity<?> response = controller.submit(request, servletRequest);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

    verify(contactRateLimiter).isAllowed("203.0.113.10");
    verify(contactService).submit(request);
  }

  @Test
  void rejectsSubmissionWhenRateLimitIsExceeded() {

    when(contactRateLimiter.isAllowed("203.0.113.10")).thenReturn(false);

    MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    servletRequest.addHeader("CF-Connecting-IP", "203.0.113.10");

    ContactRequest request = createRequest("");

    ResponseEntity<?> response = controller.submit(request, servletRequest);

    assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());

    verify(contactRateLimiter).isAllowed("203.0.113.10");
    verifyNoInteractions(contactService);
  }

  @Test
  void ignoresHoneypotSubmissionWithoutSendingEmail() {

    MockHttpServletRequest servletRequest = new MockHttpServletRequest();

    ContactRequest request = createRequest("spam.example");

    ResponseEntity<?> response = controller.submit(request, servletRequest);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

    verifyNoInteractions(contactRateLimiter, contactService);
  }

  @Test
  void usesRemoteAddressWhenCloudflareHeaderIsMissing() {

    when(contactRateLimiter.isAllowed("198.51.100.10")).thenReturn(true);

    MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    servletRequest.setRemoteAddr("198.51.100.10");

    ContactRequest request = createRequest("");

    ResponseEntity<?> response = controller.submit(request, servletRequest);

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

    verify(contactRateLimiter).isAllowed("198.51.100.10");
    verify(contactService).submit(request);
  }

  private ContactRequest createRequest(String website) {
    return new ContactRequest(
        "Joe",
        "joe@example.com",
        "Portfolio enquiry",
        "Hello, I would like to get in touch.",
        website);
  }
}
