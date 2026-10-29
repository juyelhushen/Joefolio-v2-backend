package com.juyel.joefolio_backend.contact.api;

import com.juyel.joefolio_backend.contact.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contact")
public class ContactController {

  private final ContactService contactService;

  public ContactController(ContactService contactService) {
    this.contactService = contactService;
  }

  @PostMapping
  public ResponseEntity<ContactResponse> submit(@Valid @RequestBody ContactRequest request) {

    contactService.submit(request);

    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(new ContactResponse("Your message has been sent."));
  }
}
