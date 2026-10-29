package com.juyel.joefolio_backend.contact.infrastructure.resend;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ResendEmailRequest(
    String from,
    List<String> to,
    String subject,
    @JsonProperty("reply_to") String replyTo,
    String text) {

  public ResendEmailRequest {
    to = List.copyOf(to);
  }
}
