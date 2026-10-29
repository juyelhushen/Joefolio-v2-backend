package com.juyel.joefolio_backend.contact.infrastructure.resend;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "resendClient", url = "https://api.resend.com")
public interface ResendClient {

  @PostMapping(value = "/emails", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResendEmailResponse sendEmail(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @RequestBody ResendEmailRequest request);
}
