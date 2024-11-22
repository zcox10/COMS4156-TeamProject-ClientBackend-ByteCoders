package com.bytecoders.emergencyaid.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Authenticate with PharmaId API on Application startup. */
@Component
public class PharmaidAuthStartupRunner implements CommandLineRunner {

  private final PharmaidAuthService pharmaidAuthService;

  public PharmaidAuthStartupRunner(PharmaidAuthService pharmaidAuthService) {
    this.pharmaidAuthService = pharmaidAuthService;
  }

  @Override
  public void run(String... args) {
    pharmaidAuthService.login();
  }
}