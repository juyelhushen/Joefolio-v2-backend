package com.juyel.joefolio_backend;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JoefolioBackendApplicationTests {

  @Test
  void contextLoads() {}

  @Test
  void mainStartsApplicationInNonWebMode() {
    assertDoesNotThrow(
        () ->
            JoefolioBackendApplication.main(
                new String[] {"--spring.main.web-application-type=none"}));
  }
}
