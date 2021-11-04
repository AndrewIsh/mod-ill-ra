package org.folio.service.illsubmissionstatus;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;
import org.folio.exception.HttpException;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.SubmissionStatus;
import org.folio.rest.jaxrs.model.SubmissionStatuses;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.folio.service.BaseService;
import static org.folio.config.Constants.STORAGE_SERVICE;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.folio.exception.ErrorCodes.MISMATCH_BETWEEN_ID_IN_PATH_AND_BODY;

public class IllsubmissionstatusStorageService extends BaseService implements IllsubmissionstatusService {

  private static final String storageService = STORAGE_SERVICE + "submission-statuses/";

  @Override
  @Validate
  public CompletableFuture<SubmissionStatus> createSubmissionStatus(SubmissionStatus status, Context context, Map<String, String> headers) {
    HttpClientInterface client = getHttpClient(headers);
    return handlePostRequest(JsonObject.mapFrom(status),  storageService, client, context, headers, logger)
      .thenApply(id -> JsonObject.mapFrom(status.withId(id))
        .mapTo(SubmissionStatus.class))
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
  public CompletableFuture<SubmissionStatus> getSubmissionStatusById(String id, Context context, Map<String, String> headers) {
    CompletableFuture<SubmissionStatus> future = new CompletableFuture<>();
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + id;
    handleGetRequest(endpoint, client, headers, logger)
      .thenApply(json -> json.mapTo(SubmissionStatus.class))
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
  public CompletableFuture<SubmissionStatuses> getSubmissionStatuses(int offset, int limit, String lang, Context context, Map<String, String> headers) {
    CompletableFuture<SubmissionStatuses> future = new CompletableFuture<>();
    HttpClientInterface client = getHttpClient(headers);
    handleGetRequest(storageService, client, headers, logger)
      .thenApply(json -> json.mapTo(SubmissionStatuses.class))
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
  public CompletableFuture<Void> updateSubmissionStatusById(String id, SubmissionStatus updatedStatus, Context context, Map<String, String> headers) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    if (isEmpty(updatedStatus.getId())) {
      updatedStatus.setId(id);
    } else if (!id.equals(updatedStatus.getId())) {
      future.completeExceptionally(new HttpException(422, MISMATCH_BETWEEN_ID_IN_PATH_AND_BODY.toError()));
      return future;
    }
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + id;
    handleGetRequest(endpoint, client, headers, logger)
      .thenApply(existingStatusJson -> existingStatusJson.mapTo(SubmissionStatus.class))
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
  public CompletableFuture<Void> deleteSubmissionStatusById(String id, Context context, Map<String, String> headers) {
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + id;
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
