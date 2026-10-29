package com.juyel.joefolio_backend.common.error;

import com.juyel.joefolio_backend.contact.service.ContactDeliveryUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ContactExceptionHandler {

  @ExceptionHandler(ContactDeliveryUnavailableException.class)
  public ProblemDetail handleDeliveryUnavailable(ContactDeliveryUnavailableException exception) {
    String detail =
        switch (exception.getCategory()) {
          case CONFIGURATION -> "Contact email delivery is not configured yet.";
          case INVALID_RESPONSE, RESEND_API, NETWORK ->
              "Contact email delivery is temporarily unavailable.";
        };

    return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, detail);
  }
}
