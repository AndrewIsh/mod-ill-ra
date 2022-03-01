package org.folio.config;

import org.folio.service.illrequest.IllrequestService;
import org.folio.service.illrequest.IllrequestStorageService;
import org.folio.service.illsubmissionstatus.IllsubmissionstatusService;
import org.folio.service.illsubmissionstatus.IllsubmissionstatusStorageService;
import org.folio.service.illsubmission.IllsubmissionService;
import org.folio.service.illsubmission.IllsubmissionStorageService;
import org.folio.service.illsupplingagency.IllSupplyingAgencyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
  @Bean
  public IllsubmissionService illsubmissionService() {
    return new IllsubmissionStorageService();
  }
  @Bean
  public IllrequestService illrequestService() {
    return new IllrequestStorageService();
  }
  @Bean
  public IllsubmissionstatusService illstatusService() {
    return new IllsubmissionstatusStorageService();
  }
  @Bean
  public IllSupplyingAgencyService illSupplyingAgencyService() {
    return new IllSupplyingAgencyService();
  }
}
