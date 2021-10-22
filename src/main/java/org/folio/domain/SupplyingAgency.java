package org.folio.domain;

import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.Submission;

public class SupplyingAgency {

  private final String apiUrl;

  public SupplyingAgency() {
    this.apiUrl = "http://localhost:6666/ill-connector";
  }

  public void sendRequest(Submission submission) {
    JsonObject requestBody = getRequestBody(submission.getId());
    System.out.println(this.apiUrl);
  }

  private JsonObject getRequestBody(String submissionId) {
    return new JsonObject()
      .put("actionName", "submitRequest")
      .put("entityId", submissionId)
      .put("actionPayload", "");
  }

}
