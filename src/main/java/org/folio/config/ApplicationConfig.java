package org.folio.config;

import org.folio.service.illrequest.IllrequestService;
import org.folio.service.illrequest.IllrequestStorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
  @Bean
  public IllrequestService illrequestService() {
    return new IllrequestStorageService();
  }
}
