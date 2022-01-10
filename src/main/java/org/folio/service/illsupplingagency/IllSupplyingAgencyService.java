package org.folio.service.illsupplingagency;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;
import org.folio.rest.jaxrs.model.SaRequestResponse;
import org.folio.rest.jaxrs.model.SearchResponse;
import org.folio.rest.jaxrs.model.supplying_agency_message_storage.request.SupplyingAgencyMessageStorageRequest;
import org.folio.rest.jaxrs.model.supplying_agency_message_storage.response.SupplyingAgencyMessageStorageResponse;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.folio.service.BaseService;
import static org.folio.config.Constants.CONNECTOR_CONNECT_TIMEOUT;
import static org.folio.config.Constants.CONNECTOR_RESPONSE_TIMEOUT;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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

  public CompletableFuture<SupplyingAgencyMessageStorageResponse> storeSupplierMessage(SupplyingAgencyMessageStorageRequest message, String requestId, Context context, Map<String, String> headers) {
    // TODO: Remove me, I am just hardcoding the connector port,
    // ultimately this will just be on OKAPI and we'll target by URL
    String url = "http://localhost:5555/ill-ra-storage/";
    HttpClientInterface client = getHttpClient(headers);
    return handlePostRequest(JsonObject.mapFrom(message), url + "messages", client, context, headers, logger)
      .thenApply(id -> JsonObject.mapFrom(message.withMessage(id))
          .mapTo(SupplyingAgencyMessageStorageResponse.class))
      .handle((req, t) -> {
        client.closeClient();
        if (Objects.nonNull(t)) {
          throw new CompletionException(t.getCause());
        }
        return req;
      });
  }

  public CompletableFuture<SearchResponse> sendSearch(String query, Map<String, String> headers) {
    HttpClientInterface client = getHttpClient(headers);
    CompletableFuture<SearchResponse> future = new CompletableFuture<>();
    // TODO: This header needs adding but it needs to dependent on a parameter passed in the incoming API
    // call, not hard coded
    headers.put("x-okapi-module-id", "mod-ill-connector-bldss-1.0");
    handleGetRequest("/ill-connector/search?query=" + query, client, headers, logger)
      .thenApply(json -> json.mapTo(SearchResponse.class))
      .handle((searchResponse, t) -> {
        client.closeClient();
        if (Objects.nonNull(t)) {
          future.completeExceptionally(t.getCause());
        }
        future.complete(searchResponse);
        return null;
      })
      .exceptionally(throwable -> {
        client.closeClient();
        future.completeExceptionally(throwable);
        return null;
      });
      return future;
  }

}
