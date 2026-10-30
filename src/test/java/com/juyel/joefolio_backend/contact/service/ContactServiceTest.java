package com.juyel.joefolio_backend.contact.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.juyel.joefolio_backend.contact.api.ContactRequest;
import com.juyel.joefolio_backend.contact.infrastructure.resend.ContactEmailProperties;
import com.juyel.joefolio_backend.contact.infrastructure.resend.ResendClient;
import com.juyel.joefolio_backend.contact.infrastructure.resend.ResendEmailRequest;
import com.juyel.joefolio_backend.contact.infrastructure.resend.ResendEmailResponse;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.net.ssl.SSLHandshakeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ContactServiceTest {

  @Mock private ResendClient resendClient;

  private ContactService contactService;

  private final ContactEmailProperties enabledProperties =
      new ContactEmailProperties(
          true, "test-api-key", "Joefolio <onboarding@resend.dev>", "owner@example.com");

  @BeforeEach
  void setUp() {
    contactService = new ContactService(resendClient, enabledProperties);
  }

  @Test
  void shouldSendContactEmailWithExpectedDetails() {
    ContactRequest request =
        new ContactRequest(
            "Joe",
            "visitor@example.com",
            "Job opportunity",
            "I would like to discuss a role.",
            "https://example.com");
    String expectedAuthorization = "Bearer " + enabledProperties.apiKey();

    when(resendClient.sendEmail(eq(expectedAuthorization), any(ResendEmailRequest.class)))
        .thenReturn(new ResendEmailResponse("email_test_123"));

    contactService.submit(request);

    ArgumentCaptor<ResendEmailRequest> captor = ArgumentCaptor.forClass(ResendEmailRequest.class);

    verify(resendClient).sendEmail(eq(expectedAuthorization), captor.capture());

    ResendEmailRequest email = captor.getValue();

    assertEquals("Joefolio <onboarding@resend.dev>", email.from());
    assertEquals(java.util.List.of("owner@example.com"), email.to());
    assertEquals("Job opportunity", email.subject());
    assertEquals("visitor@example.com", email.replyTo());
    assertTrue(email.text().contains("Joe"));
    assertTrue(email.text().contains("I would like to discuss a role."));
  }

  @Test
  void shouldRejectRequestWhenEmailDeliveryIsDisabled() {
    ContactService disabledService =
        new ContactService(resendClient, new ContactEmailProperties(false, "", "", ""));

    ContactRequest request =
        new ContactRequest(
            "Joe", "visitor@example.com", "Hello", "Test message", "https://example.com");

    assertThrows(ContactDeliveryUnavailableException.class, () -> disabledService.submit(request));

    verify(resendClient, never()).sendEmail(any(String.class), any(ResendEmailRequest.class));
  }

  @Test
  void shouldRejectRequestWhenRequiredConfigurationIsMissing() {
    ContactService incompleteService =
        new ContactService(resendClient, new ContactEmailProperties(true, " ", " ", " "));

    ContactRequest request =
        new ContactRequest(
            "Joe", "visitor@example.com", "Hello", "Test message", "https://example.com");

    assertThrows(
        ContactDeliveryUnavailableException.class, () -> incompleteService.submit(request));
    verify(resendClient, never()).sendEmail(any(String.class), any(ResendEmailRequest.class));
  }

  @Test
  void shouldClassifyFeignFailuresAndReturnDeepestCause() throws Exception {
    Method classify = ContactService.class.getDeclaredMethod("classifyFeignFailure", int.class);
    classify.setAccessible(true);

    assertEquals(
        "Resend rejected the API key or authentication.", classify.invoke(contactService, 401));
    assertEquals("Resend rejected the sender or API access.", classify.invoke(contactService, 403));
    assertEquals(
        "Resend rejected the request payload or recipient details.",
        classify.invoke(contactService, 400));
    assertEquals(
        "Resend rejected the request payload or recipient details.",
        classify.invoke(contactService, 422));
    assertEquals("Resend returned an upstream error.", classify.invoke(contactService, 500));

    Method deepestCause = ContactService.class.getDeclaredMethod("deepestCause", Throwable.class);
    deepestCause.setAccessible(true);

    RuntimeException root = new RuntimeException("root cause");
    RuntimeException intermediate = new RuntimeException("mid", root);
    RuntimeException top = new RuntimeException("top", intermediate);

    assertEquals(root, deepestCause.invoke(contactService, top));

    RuntimeException single = new RuntimeException("single");
    assertEquals(single, deepestCause.invoke(contactService, single));
  }

  @Test
  void shouldExposeConfigurationAndNullNetworkFailureDetails() {
    ContactDeliveryUnavailableException configFailure = new ContactDeliveryUnavailableException();
    assertEquals(
        ContactDeliveryUnavailableException.FailureCategory.CONFIGURATION,
        configFailure.getCategory());
    assertEquals(null, configFailure.getUpstreamStatus());
    assertEquals(null, configFailure.getRootCauseClass());
    assertEquals(null, configFailure.getRootCauseMessage());

    ContactDeliveryUnavailableException nullNetworkFailure =
        ContactDeliveryUnavailableException.networkFailure(null);
    assertEquals(
        ContactDeliveryUnavailableException.FailureCategory.NETWORK,
        nullNetworkFailure.getCategory());
    assertEquals("unknown", nullNetworkFailure.getRootCauseClass());
    assertEquals(null, nullNetworkFailure.getRootCauseMessage());
  }

  @Test
  void shouldRejectResponseWithoutEmailId() {
    ContactRequest request =
        new ContactRequest(
            "Joe", "visitor@example.com", "Hello", "Test message", "https://example.com");
    String expectedAuthorization = "Bearer " + enabledProperties.apiKey();

    when(resendClient.sendEmail(eq(expectedAuthorization), any(ResendEmailRequest.class)))
        .thenReturn(new ResendEmailResponse(" "));

    assertThrows(ContactDeliveryUnavailableException.class, () -> contactService.submit(request));
  }

  @Test
  void shouldRejectResendApiAuthenticationFailure() {
    ContactRequest request =
        new ContactRequest(
            "Joe", "visitor@example.com", "Hello", "Test message", "https://example.com");
    String expectedAuthorization = "Bearer " + enabledProperties.apiKey();

    when(resendClient.sendEmail(eq(expectedAuthorization), any(ResendEmailRequest.class)))
        .thenThrow(
            FeignException.errorStatus(
                "sendEmail",
                Response.builder()
                    .status(401)
                    .reason("Unauthorized")
                    .request(
                        Request.create(
                            Request.HttpMethod.POST,
                            "https://api.resend.com/emails",
                            Map.of(
                                "Authorization",
                                java.util.Collections.singletonList(expectedAuthorization)),
                            new byte[0],
                            StandardCharsets.UTF_8))
                    .build()));

    ContactDeliveryUnavailableException exception =
        assertThrows(
            ContactDeliveryUnavailableException.class, () -> contactService.submit(request));

    assertEquals(
        ContactDeliveryUnavailableException.FailureCategory.RESEND_API, exception.getCategory());
    assertEquals(401, exception.getUpstreamStatus());
  }

  @Test
  void shouldRejectResendTransportFailure() {
    ContactRequest request =
        new ContactRequest(
            "Joe", "visitor@example.com", "Hello", "Test message", "https://example.com");
    String expectedAuthorization = "Bearer " + enabledProperties.apiKey();
    Request feignRequest =
        Request.create(
            Request.HttpMethod.POST,
            "https://api.resend.com/emails",
            Map.of("Authorization", java.util.Collections.singletonList(expectedAuthorization)),
            new byte[0],
            StandardCharsets.UTF_8);

    SSLHandshakeException sslCause =
        new SSLHandshakeException(
            "PKIX path building failed: unable to find valid certification path to requested target");

    when(resendClient.sendEmail(eq(expectedAuthorization), any(ResendEmailRequest.class)))
        .thenThrow(
            new RetryableException(
                -1,
                "Connection refused",
                Request.HttpMethod.POST,
                sslCause,
                (Long) null,
                feignRequest));

    ContactDeliveryUnavailableException exception =
        assertThrows(
            ContactDeliveryUnavailableException.class, () -> contactService.submit(request));

    assertEquals(
        ContactDeliveryUnavailableException.FailureCategory.NETWORK, exception.getCategory());
    assertEquals(null, exception.getUpstreamStatus());
    assertEquals("SSLHandshakeException", exception.getRootCauseClass());
    assertTrue(exception.getRootCauseMessage().contains("PKIX path building failed"));
  }
}
