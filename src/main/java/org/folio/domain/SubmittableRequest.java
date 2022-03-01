package org.folio.domain;

import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.Request;
import org.folio.rest.jaxrs.model.SaRequestRequest;
import org.folio.rest.jaxrs.model.Submission;

public class SubmittableRequest {

  private final JsonObject jsonRequest;
  private final Submission submission;
  private final Request request;

  public SubmittableRequest(Submission submission, SaRequestRequest requestPayload) {
    this.submission = submission;
    this.jsonRequest = JsonObject.mapFrom(requestPayload);
    this.request = new Request();
  }

  public Request build() {
    return this.request
      .withSubmissionId(this.submission.getId());
  }
}
