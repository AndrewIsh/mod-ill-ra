package org.folio.domain;

import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.SaRequestRequest;

public class SupplyingAgency {

  public JsonObject buildRequest(SaRequestRequest request) {
    return getRequestBody(request);
  }

  private JsonObject getRequestBody(SaRequestRequest request) {
    return new JsonObject()
      .put("actionName", "submitRequest")
      .put("actionMetadata", request.getSaRequestMetadata());
  }

}
