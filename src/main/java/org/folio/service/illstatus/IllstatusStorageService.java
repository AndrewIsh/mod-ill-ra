package org.folio.service.illstatus;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;
import org.folio.exception.HttpException;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Status;
import org.folio.rest.jaxrs.model.Statuses;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.folio.service.BaseService;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.folio.exception.ErrorCodes.MISMATCH_BETWEEN_ID_IN_PATH_AND_BODY;

public class IllstatusStorageService extends BaseService implements IllstatusService {

  private static final String storageService = "/ill-ra-storage/";

  @Override
  @Validate
  public CompletableFuture<Status> createStatus(Status status, Context context, Map<String, String> headers) {
    HttpClientInterface client = getHttpClient(headers);
    return handlePostRequest(JsonObject.mapFrom(status),  storageService + "statuses", client, context, headers, logger)
      .thenApply(id -> JsonObject.mapFrom(status.withId(id))
        .mapTo(Status.class))
      .handle((stat, t) -> {
        client.closeClient();
        if (Objects.nonNull(t)) {
          throw new CompletionException(t.getCause());
        }
        return stat;
      });
  }

  @Override
  @Validate
  public CompletableFuture<Status> getStatusById(String id, Context context, Map<String, String> headers) {
    CompletableFuture<Status> future = new CompletableFuture<>();
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + "statuses/" + id;
    handleGetRequest(endpoint, client, headers, logger)
      .thenApply(json -> json.mapTo(Status.class))
      .handle((stat, t) -> {
        client.closeClient();
        if (Objects.nonNull(t)) {
          future.completeExceptionally(t.getCause());
        } else {
          future.complete(stat);
        }
        return null;
      });
    return future;
  }

  @Override
  @Validate
  public CompletableFuture<Statuses> getStatuses(int offset, int limit, String lang, Context context, Map<String, String> headers) {
    CompletableFuture<Statuses> future = new CompletableFuture<>();
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + "statuses";
    handleGetRequest(endpoint, client, headers, logger)
      .thenApply(json -> json.mapTo(Statuses.class))
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
  public CompletableFuture<Void> updateStatusById(String id, Status updatedStatus, Context context, Map<String, String> headers) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    if (isEmpty(updatedStatus.getId())) {
      updatedStatus.setId(id);
    } else if (!id.equals(updatedStatus.getId())) {
      future.completeExceptionally(new HttpException(422, MISMATCH_BETWEEN_ID_IN_PATH_AND_BODY.toError()));
      return future;
    }
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + "statuses/" + id;
    handleGetRequest(endpoint, client, headers, logger)
      .thenApply(existingStatusJson -> existingStatusJson.mapTo(Status.class))
      .thenAccept(ok -> handlePutRequest(endpoint, JsonObject.mapFrom(updatedStatus), client, headers, logger)
        .handle((stat, t) -> {
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
  public CompletableFuture<Void> deleteStatusById(String id, Context context, Map<String, String> headers) {
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + "statuses/" + id;
    return handleDeleteRequest(endpoint, client, headers, logger)
      .handle((stat, t) -> {
        client.closeClient();
        if (Objects.nonNull(t)) {
          throw new CompletionException(t.getCause());
        }
        return null;
      });

  }
}
