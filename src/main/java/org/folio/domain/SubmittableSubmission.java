package org.folio.domain;

import org.folio.rest.jaxrs.model.SaRequestRequest;
import org.folio.rest.jaxrs.model.Submission;
import org.folio.rest.jaxrs.model.SubmissionMetadata;
import org.folio.util.DateTimeUtils;
import static org.folio.config.Constants.ISO18626_DATE_FORMAT;

import java.time.ZonedDateTime;

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
      .withSubmissionDate(DateTimeUtils.dtToString(ZonedDateTime.now(), ISO18626_DATE_FORMAT))
      // TODO: This will be a default value
      .withStatusId("49600f35-e2af-45ef-a03c-ee0de7ec3c89");
  }

}
