package org.folio.domain;

import org.folio.rest.jaxrs.model.SaRequestRequest;
import org.folio.rest.jaxrs.model.Submission;
import org.folio.rest.jaxrs.model.SubmissionMetadata;

import java.util.Date;

public class SubmittableSubmission {

  private final SubmissionMetadata metadata;
  private final Submission submission;

  public SubmittableSubmission(SaRequestRequest requestPayload) {
    // Submission metadata only uses bibliographic & publication info
    this.metadata = new SubmissionMetadata()
      .withBibliographicInfo(requestPayload.getSaRequestMetadata().getBibliographicInfo())
      .withPublicationInfo(requestPayload.getSaRequestMetadata().getPublicationInfo());
    this.submission = new Submission();
  }

  public Submission build() {
   return this.submission
      .withSubmissionMetadata(this.metadata)
      .withSubmissionDate(new Date())
      // TODO: This will be provided by FOLIO
      .withSubmissionLocation("f31216de-d13a-49d3-b6bd-c6757264a22d")
      // TODO: This will be a default value
      .withStatusId("f1ec6c6e-d2d0-4d9c-88bb-548b7e1850c1")
      // TODO: THis will be provided by FOLIO
      .withUserId("70993f3b-c9e8-4b16-9f5a-21102b39bdcc");
  }

}
