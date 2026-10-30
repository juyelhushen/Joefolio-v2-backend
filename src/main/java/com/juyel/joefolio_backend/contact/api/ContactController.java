package com.juyel.joefolio_backend.contact.api;

import com.juyel.joefolio_backend.contact.service.ContactRateLimiter;
import com.juyel.joefolio_backend.contact.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
public class ContactController {

  private final ContactService contactService;
  private final ContactRateLimiter rateLimiter;

  @PostMapping
  public ResponseEntity<ContactResponse> submit(
      @Valid @RequestBody ContactRequest request, HttpServletRequest servletRequest) {

    // Silently ignore automated submissions caught by the honeypot.
    if (request.website() != null && !request.website().isBlank()) {
      return acceptedResponse();
    }

    String clientIp = resolveClientIp(servletRequest);

    // Allow only five submissions per client in ten minutes.
    if (!rateLimiter.isAllowed(clientIp)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .body(new ContactResponse("Too many submissions. Please try again later."));
    }

    contactService.submit(request);
    return acceptedResponse();
  }

  private String resolveClientIp(HttpServletRequest request) {
    String cloudflareIp = request.getHeader("CF-Connecting-IP");

    if (cloudflareIp != null
        && !cloudflareIp.isBlank()
        && cloudflareIp.length() <= 64
        && !cloudflareIp.contains(",")) {
      return cloudflareIp.trim();
    }

    // Used during local development when the Cloudflare header is absent.
    return request.getRemoteAddr();
  }

  private ResponseEntity<ContactResponse> acceptedResponse() {
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(new ContactResponse("Your message has been sent."));
  }
}
