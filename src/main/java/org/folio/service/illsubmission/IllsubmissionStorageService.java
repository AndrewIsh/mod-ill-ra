package org.folio.service.illsubmission;

import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
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

  private static final String STORAGE_SERVICE = "/ill-ra-storage/submissions";
  private static final String SEARCH_PARAMS = "?limit=%s&offset=%s%s&lang=%s";

  @Override
  @Validate
  public CompletableFuture<Submission> createSubmission(Submission submission, Context context, Map<String, String> headers) {
    HttpClientInterface client = getHttpClient(headers);
    return handlePostRequest(JsonObject.mapFrom(submission), STORAGE_SERVICE, client, context, headers, logger)
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
    String endpoint = STORAGE_SERVICE + "/" + id;
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
  public CompletableFuture<Submissions> getSubmissions(int offset, int limit, String query, String lang, Context context, Map<String, String> headers) {
    CompletableFuture<Submissions> future = new CompletableFuture<>();
    HttpClientInterface client = getHttpClient(headers);
    String endpoint = StringUtils.isEmpty(query) ?
      String.format(STORAGE_SERVICE + SEARCH_PARAMS, limit, offset, buildQuery(query, logger), lang) :
      String.format(STORAGE_SERVICE + SEARCH_PARAMS, limit, offset, buildQuery(combineCqlExpressions("and", query), logger), lang);
    handleGetRequest(endpoint, client, headers, logger)
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
    String endpoint = STORAGE_SERVICE + "/" + id;
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
    String endpoint = STORAGE_SERVICE + "/" + id;
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
    String endpoint = STORAGE_SERVICE + "/" + id + "/requests";
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
