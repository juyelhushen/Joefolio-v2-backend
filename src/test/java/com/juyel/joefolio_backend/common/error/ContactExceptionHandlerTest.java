package com.juyel.joefolio_backend.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.juyel.joefolio_backend.contact.service.ContactDeliveryUnavailableException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class ContactExceptionHandlerTest {

  private final ContactExceptionHandler handler = new ContactExceptionHandler();

  @Test
  void shouldReportConfigurationFailuresAsServiceUnavailable() {
    ProblemDetail detail =
        handler.handleDeliveryUnavailable(
            ContactDeliveryUnavailableException.configurationFailure());

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), detail.getStatus());
    assertEquals("Contact email delivery is not configured yet.", detail.getDetail());
  }

  @Test
  void shouldReportTemporaryDeliveryFailuresAsServiceUnavailable() {
    List<ProblemDetail> details =
        List.of(
            handler.handleDeliveryUnavailable(
                ContactDeliveryUnavailableException.invalidResponse("missing delivery id")),
            handler.handleDeliveryUnavailable(
                ContactDeliveryUnavailableException.upstreamFailure(503)),
            handler.handleDeliveryUnavailable(
                ContactDeliveryUnavailableException.networkFailure(new RuntimeException("ssl"))));

    for (ProblemDetail detail : details) {
      assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), detail.getStatus());
      assertEquals("Contact email delivery is temporarily unavailable.", detail.getDetail());
    }
  }

  @Test
  void shouldMarkUnknownNetworkCauseWhenRootCauseIsMissing() {
    ContactDeliveryUnavailableException exception =
        ContactDeliveryUnavailableException.networkFailure(null);

    assertEquals("unknown", exception.getRootCauseClass());
    assertEquals(null, exception.getRootCauseMessage());
  }
}
