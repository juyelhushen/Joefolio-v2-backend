package com.juyel.joefolio_backend.contact.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
    @NotBlank(message = "Name is required") @Size(max = 80, message = "Name must not exceed 80 characters") String name,
    @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address") @Size(max = 254, message = "Email must not exceed 254 characters") String email,
    @NotBlank(message = "Subject is required") @Size(max = 150, message = "Subject must not exceed 150 characters") String subject,
    @NotBlank(message = "Message body is required") @Size(max = 5000, message = "Message must not exceed 5000 characters") String body) {}
