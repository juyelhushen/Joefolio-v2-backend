package com.juyel.joefolio_backend.contact.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContactRateLimiterTest {

  private MutableClock clock;
  private ContactRateLimiter rateLimiter;

  @BeforeEach
  void setUp() {
    clock = new MutableClock(Instant.parse("2026-10-30T10:00:00Z"));

    rateLimiter = new ContactRateLimiter(clock);
  }

  @Test
  void allowsFiveRequestsAndRejectsSixthRequest() {

    for (int i = 0; i < 5; i++) {
      assertTrue(rateLimiter.isAllowed("192.168.1.10"));
    }

    assertFalse(rateLimiter.isAllowed("192.168.1.10"));
  }

  @Test
  void tracksDifferentClientsIndependently() {

    for (int i = 0; i < 5; i++) {
      assertTrue(rateLimiter.isAllowed("192.168.1.10"));
    }

    assertFalse(rateLimiter.isAllowed("192.168.1.10"));

    assertTrue(rateLimiter.isAllowed("192.168.1.20"));
  }

  @Test
  void resetsLimitAfterTenMinutes() {

    for (int i = 0; i < 5; i++) {
      assertTrue(rateLimiter.isAllowed("192.168.1.10"));
    }

    assertFalse(rateLimiter.isAllowed("192.168.1.10"));

    clock.advance(Duration.ofMinutes(10));

    assertTrue(rateLimiter.isAllowed("192.168.1.10"));
  }

  private static final class MutableClock extends Clock {

    private Instant currentInstant;

    private MutableClock(Instant initialInstant) {
      this.currentInstant = initialInstant;
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return currentInstant;
    }

    private void advance(Duration duration) {
      currentInstant = currentInstant.plus(duration);
    }
  }
}
