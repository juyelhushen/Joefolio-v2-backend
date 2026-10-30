package com.juyel.joefolio_backend.contact.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ContactRateLimiter {

  private static final int MAX_REQUESTS = 5;
  private static final Duration WINDOW = Duration.ofMinutes(10);
  private static final int MAX_TRACKED_CLIENTS = 10_000;

  private final Clock clock;

  // Access-ordered map allows us to evict the least recently used client.
  private final Map<String, Window> clients = new LinkedHashMap<>(16, 0.75f, true);

  public ContactRateLimiter() {
    this.clock = Clock.systemUTC();
  }

  ContactRateLimiter(Clock clock) {
    this.clock = clock;
  }

  public synchronized boolean isAllowed(String clientKey) {

    String key = (clientKey == null || clientKey.isBlank()) ? "unknown" : clientKey.trim();

    Instant now = clock.instant();
    Window current = clients.get(key);

    // Start a new window for a new client or an expired window.
    if (current == null || !now.isBefore(current.startedAt().plus(WINDOW))) {

      if (current == null && clients.size() >= MAX_TRACKED_CLIENTS) {
        evictLeastRecentlyUsed();
      }

      clients.put(key, new Window(now, 1));
      return true;
    }

    // Reject requests after the fifth request in the current window.
    if (current.requestCount() >= MAX_REQUESTS) {
      return false;
    }

    clients.put(key, new Window(current.startedAt(), current.requestCount() + 1));

    return true;
  }

  private void evictLeastRecentlyUsed() {
    Iterator<String> iterator = clients.keySet().iterator();

    if (iterator.hasNext()) {
      iterator.next();
      iterator.remove();
    }
  }

  private record Window(Instant startedAt, int requestCount) {}
}
