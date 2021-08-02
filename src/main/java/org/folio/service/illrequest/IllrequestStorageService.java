package org.folio.service.illrequest;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;
import org.folio.exception.HttpException;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Request;
import org.folio.rest.jaxrs.model.Requests;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.folio.service.BaseService;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.folio.exception.ErrorCodes.MISMATCH_BETWEEN_ID_IN_PATH_AND_BODY;

public class IllrequestStorageService extends BaseService implements IllrequestService {

  private static final String storageService = "/ill-ra-storage/";

  @Override
  @Validate
  public CompletableFuture<Request> createRequest(Request request, Context context, Map<String, String> headers) {
    HttpClientInterface client = getHttpClient(headers);
    return handlePostRequest(JsonObject.mapFrom(request),  storageService + "requests", client, context, headers, logger)
      .thenApply(id -> JsonObject.mapFrom(request.withId(id))
        .mapTo(Request.class))
      .handle((req, t) -> {
        client.closeClient();
        if (Objects.nonNull(t)) {
          throw new CompletionException(t.getCause());
        }
        return req;
      });
  }

  @Override
  @Validate
  public CompletableFuture<Request> getRequestById(String id, Context context, Map<String, String> headers) {
    CompletableFuture<Request> future = new CompletableFuture<>();
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + "requests/" + id;
    handleGetRequest(endpoint, client, headers, logger)
      .thenApply(json -> json.mapTo(Request.class))
      .handle((request, t) -> {
        client.closeClient();
        if (Objects.nonNull(t)) {
          future.completeExceptionally(t.getCause());
        } else {
          future.complete(request);
        }
        return null;
      });
    return future;
  }

  @Override
  @Validate
  public CompletableFuture<Requests> getRequests(int offset, int limit, String lang, String query, Context context, Map<String, String> headers) {
    CompletableFuture<Requests> future = new CompletableFuture<>();
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + "requests";
    handleGetRequest(endpoint, client, headers, logger)
      .thenApply(json -> json.mapTo(Requests.class))
      .handle((collection, t) -> {
        client.closeClient();
        if (Objects.nonNull(t)) {
          future.completeExceptionally(t.getCause());
        } else {
          future.complete(collection);
        }
        return null;
      });
    return future;
  }

  @Override
  @Validate
  public CompletableFuture<Void> updateRequestById(String id, Request updatedRequest, Context context, Map<String, String> headers) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    if (isEmpty(updatedRequest.getId())) {
      updatedRequest.setId(id);
    } else if (!id.equals(updatedRequest.getId())) {
      future.completeExceptionally(new HttpException(422, MISMATCH_BETWEEN_ID_IN_PATH_AND_BODY.toError()));
      return future;
    }
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + "requests/" + id;
    handleGetRequest(endpoint, client, headers, logger)
      .thenApply(existingRequestJson -> existingRequestJson.mapTo(Request.class))
      .thenAccept(ok -> handlePutRequest(endpoint, JsonObject.mapFrom(updatedRequest), client, headers, logger)
        .handle((req, t) -> {
          client.closeClient();
          if (Objects.nonNull(t)) {
            future.completeExceptionally(t);
          } else {
            future.complete(null);
          }
          return null;
        }))
    .exceptionally(t -> {
      future.completeExceptionally(t.getCause());
      return null;
    });
    return future;
  }

  @Override
  @Validate
  public CompletableFuture<Void> deleteRequestById(String id, Context context, Map<String, String> headers) {
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + "requests/" + id;
    return handleDeleteRequest(endpoint, client, headers, logger)
      .handle((req, t) -> {
        client.closeClient();
        if (Objects.nonNull(t)) {
         throw new CompletionException(t.getCause());
        }
        return null;
      });
  }
}
