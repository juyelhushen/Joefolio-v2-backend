package com.juyel.joefolio_backend.contact.infrastructure.resend;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.contact.email")
public record ContactEmailProperties(boolean enabled, String apiKey, String from, String to) {}
