package com.juyel.joefolio_backend.contact.service;

public class ContactDeliveryUnavailableException extends RuntimeException {
  public enum FailureCategory {
    CONFIGURATION,
    RESEND_API,
    NETWORK,
    INVALID_RESPONSE
  }

  private final FailureCategory category;
  private final Integer upstreamStatus;
  private final String rootCauseClass;
  private final String rootCauseMessage;

  public ContactDeliveryUnavailableException() {
    this(
        "Contact email delivery is not configured yet.",
        FailureCategory.CONFIGURATION,
        null,
        null,
        null);
  }

  public ContactDeliveryUnavailableException(
      String message,
      FailureCategory category,
      Integer upstreamStatus,
      String rootCauseClass,
      String rootCauseMessage) {
    super(message);
    this.category = category;
    this.upstreamStatus = upstreamStatus;
    this.rootCauseClass = rootCauseClass;
    this.rootCauseMessage = rootCauseMessage;
  }

  public static ContactDeliveryUnavailableException configurationFailure() {
    return new ContactDeliveryUnavailableException(
        "Contact email delivery is not configured yet.",
        FailureCategory.CONFIGURATION,
        null,
        null,
        null);
  }

  public static ContactDeliveryUnavailableException upstreamFailure(Integer upstreamStatus) {
    return new ContactDeliveryUnavailableException(
        "Contact email delivery is temporarily unavailable.",
        FailureCategory.RESEND_API,
        upstreamStatus,
        null,
        null);
  }

  public static ContactDeliveryUnavailableException networkFailure(Throwable cause) {
    String rootCauseClass = cause == null ? "unknown" : cause.getClass().getSimpleName();
    String rootCauseMessage = cause == null ? null : cause.getMessage();
    return new ContactDeliveryUnavailableException(
        "Contact email delivery is temporarily unavailable.",
        FailureCategory.NETWORK,
        null,
        rootCauseClass,
        rootCauseMessage);
  }

  public static ContactDeliveryUnavailableException invalidResponse(String message) {
    return new ContactDeliveryUnavailableException(
        message, FailureCategory.INVALID_RESPONSE, null, null, null);
  }

  public FailureCategory getCategory() {
    return category;
  }

  public Integer getUpstreamStatus() {
    return upstreamStatus;
  }

  public String getRootCauseClass() {
    return rootCauseClass;
  }

  public String getRootCauseMessage() {
    return rootCauseMessage;
  }
}
