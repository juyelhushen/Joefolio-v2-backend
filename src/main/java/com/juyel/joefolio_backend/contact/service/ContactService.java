package com.juyel.joefolio_backend.contact.service;

import com.juyel.joefolio_backend.contact.api.ContactRequest;
import com.juyel.joefolio_backend.contact.infrastructure.resend.ContactEmailProperties;
import com.juyel.joefolio_backend.contact.infrastructure.resend.ResendClient;
import com.juyel.joefolio_backend.contact.infrastructure.resend.ResendEmailRequest;
import com.juyel.joefolio_backend.contact.infrastructure.resend.ResendEmailResponse;
import feign.FeignException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

  private final ResendClient resendClient;
  private final ContactEmailProperties properties;

  public void submit(ContactRequest request) {
    validateConfiguration();

    try {
      ResendEmailResponse response =
          resendClient.sendEmail("Bearer " + properties.apiKey(), buildEmailRequest(request));
      validateResponse(response);
      logAcceptedEmail();
    } catch (FeignException exception) {
      handleFeignException(exception);
    }
  }

  private ResendEmailRequest buildEmailRequest(ContactRequest request) {
    String body =
        String.join(
            System.lineSeparator(),
            "New contact message from your Joefolio portfolio.",
            "",
            "Name: " + request.name(),
            "Email: " + request.email(),
            "Subject: " + request.subject(),
            "",
            "Message:",
            request.body());

    return new ResendEmailRequest(
        properties.from(), List.of(properties.to()), request.subject(), request.email(), body);
  }

  private void validateResponse(ResendEmailResponse response) {
    if (response == null || !StringUtils.hasText(response.id())) {
      throw ContactDeliveryUnavailableException.invalidResponse(
          "Resend did not return a valid delivery id.");
    }
  }

  private void handleFeignException(FeignException exception) {
    if (exception.status() >= 0) {
      if (log.isWarnEnabled()) {
        log.warn(
            "Resend API failure detected: category={}, httpStatus={}, reason={}.",
            ContactDeliveryUnavailableException.FailureCategory.RESEND_API,
            exception.status(),
            classifyFeignFailure(exception.status()));
      }
      throw ContactDeliveryUnavailableException.upstreamFailure(exception.status());
    }

    Throwable rootCause = deepestCause(exception);
    String rootCauseClass = rootCause.getClass().getSimpleName();
    String rootCauseMessage = rootCause.getMessage();
    if (log.isWarnEnabled()) {
      log.warn(
          "Resend transport failure detected: category={}, causeType={}, causeMessage={}.",
          ContactDeliveryUnavailableException.FailureCategory.NETWORK,
          rootCauseClass,
          rootCauseMessage);
    }
    throw ContactDeliveryUnavailableException.networkFailure(rootCause);
  }

  private void logAcceptedEmail() {
    if (log.isInfoEnabled()) {
      log.info("Resend accepted a Joefolio contact email.");
    }
  }

  private void validateConfiguration() {
    if (!properties.enabled()
        || !StringUtils.hasText(properties.apiKey())
        || !StringUtils.hasText(properties.from())
        || !StringUtils.hasText(properties.to())) {
      throw ContactDeliveryUnavailableException.configurationFailure();
    }
  }

  private String classifyFeignFailure(int status) {
    if (status == 401) {
      return "Resend rejected the API key or authentication.";
    }
    if (status == 403) {
      return "Resend rejected the sender or API access.";
    }
    if (status == 400 || status == 422) {
      return "Resend rejected the request payload or recipient details.";
    }
    return "Resend returned an upstream error.";
  }

  private Throwable deepestCause(Throwable throwable) {
    Throwable current = throwable;
    while (current.getCause() != null && current.getCause() != current) {
      current = current.getCause();
    }
    return current;
  }
}
