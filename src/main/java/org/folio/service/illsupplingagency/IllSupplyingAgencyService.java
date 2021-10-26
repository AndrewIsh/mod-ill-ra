package org.folio.service.illsupplingagency;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.SaRequestResponse;
import org.folio.service.BaseService;
import static org.folio.config.Constants.CONNECTOR_CONNECT_TIMEOUT;
import static org.folio.config.Constants.CONNECTOR_RESPONSE_TIMEOUT;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class IllSupplyingAgencyService extends BaseService {

  // TODO: Remove me, I am just hardcoding the connector port,
  // ultimately this will just be on OKAPI and we'll target by connector
  private static final String connectorApi = "http://localhost:7777/ill-connector";

  public CompletableFuture<SaRequestResponse> sendSupplierRequest(JsonObject submission, Context context, Map<String, String> headers) {
    // TODO: Remove me, I am just here to allow the connection to the connector
    // to be made on the non-OKAPI port during dev
    headers.remove("x-okapi-url");
    //CompletableFuture<SaRequestResponse> future = new CompletableFuture<>();
    HttpClient client = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(CONNECTOR_CONNECT_TIMEOUT))
      .build();
    HttpRequest.Builder request = HttpRequest.newBuilder()
      .uri(URI.create(connectorApi + "/action"))
      .timeout(Duration.ofSeconds(CONNECTOR_RESPONSE_TIMEOUT))
      .POST(HttpRequest.BodyPublishers.ofString(JsonObject.mapFrom(submission).toString()));

    // Add our existing headers
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      request.header(entry.getKey(), entry.getValue());
    }
    // Add additional missing headers
    request.header("Content-type", "application/json");
    request.header("Accept", "application/json");

    HttpRequest builtRequest = request.build();
    // Send the request, receive the response, convert it into a response object
    // then complete the future with it
    CompletableFuture<HttpResponse<String>> future = client.sendAsync(builtRequest, HttpResponse.BodyHandlers.ofString());
    return future.thenApply(apiResponse -> new JsonObject(apiResponse.body()).mapTo(SaRequestResponse.class));
  }
}
