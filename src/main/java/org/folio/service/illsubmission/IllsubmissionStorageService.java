package org.folio.service.illsubmission;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;
import org.folio.exception.HttpException;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Requests;
import org.folio.rest.jaxrs.model.Submission;
import org.folio.rest.jaxrs.model.Submissions;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.folio.service.BaseService;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.folio.exception.ErrorCodes.MISMATCH_BETWEEN_ID_IN_PATH_AND_BODY;

public class IllsubmissionStorageService extends BaseService implements IllsubmissionService {

  private static final String storageService = "/ill-ra-storage/submissions";

  @Override
  @Validate
  public CompletableFuture<Submission> createSubmission(Submission submission, Context context, Map<String, String> headers) {
    HttpClientInterface client = getHttpClient(headers);
    return handlePostRequest(JsonObject.mapFrom(submission), storageService, client, context, headers, logger)
      .thenApply(id -> JsonObject.mapFrom(submission.withId(id))
        .mapTo(Submission.class))
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
  public CompletableFuture<Submission> getSubmissionById(String id, Context context, Map<String, String> headers) {
    CompletableFuture<Submission> future = new CompletableFuture<>();
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + "/" + id;
    handleGetRequest(endpoint, client, headers, logger)
      .thenApply(json -> json.mapTo(Submission.class))
      .handle((submission, t) -> {
        client.closeClient();
        if (Objects.nonNull(t)) {
          future.completeExceptionally(t.getCause());
        } else {
          future.complete(submission);
        }
        return null;
      });
    return future;
  }

  @Override
  @Validate
  public CompletableFuture<Submissions> getSubmissions(int offset, int limit, String lang, String query, Context context, Map<String, String> headers) {
    CompletableFuture<Submissions> future = new CompletableFuture<>();
    HttpClientInterface client = getHttpClient(headers);
    handleGetRequest(storageService, client, headers, logger)
      .thenApply(json -> json.mapTo(Submissions.class))
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
  public CompletableFuture<Void> updateSubmissionById(String id, Submission updatedSubmission, Context context, Map<String, String> headers) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    if (isEmpty(updatedSubmission.getId())) {
      updatedSubmission.setId(id);
    } else if (!id.equals(updatedSubmission.getId())) {
      future.completeExceptionally(new HttpException(422, MISMATCH_BETWEEN_ID_IN_PATH_AND_BODY.toError()));
      return future;
    }
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + "/" + id;
    handleGetRequest(endpoint, client, headers, logger)
      .thenApply(existingSubmissionJson -> existingSubmissionJson.mapTo(Submission.class))
      .thenAccept(ok -> handlePutRequest(endpoint, JsonObject.mapFrom(updatedSubmission), client, headers, logger)
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
  public CompletableFuture<Void> deleteSubmissionById(String id, Context context, Map<String, String> headers) {
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + "/" + id;
    return handleDeleteRequest(endpoint, client, headers, logger)
      .handle((sub, t) -> {
        client.closeClient();
        if (Objects.nonNull(t)) {
          throw new CompletionException(t.getCause());
        }
        return null;
      });
  }

  @Override
  public CompletableFuture<Requests> getSubmissionRequestsById(String id, Context context, Map<String, String> headers) {
    CompletableFuture<Requests> future = new CompletableFuture<>();
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = storageService + "/" + id + "/requests";
    handleGetRequest(endpoint, client, headers, logger)
      .thenApply(json -> json.mapTo(Requests.class))
      .handle((requests, t) -> {
        client.closeClient();
        if (Objects.nonNull(t)) {
          future.completeExceptionally(t.getCause());
        } else {
          future.complete(requests);
        }
        return null;
      });
    return future;
  }
}
