package org.folio.service.illsubmission;

import io.vertx.core.Context;
import org.folio.rest.jaxrs.model.Submission;
import org.folio.rest.jaxrs.model.Submissions;
import org.folio.rest.jaxrs.model.Requests;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IllsubmissionService {

  /**
   * This method creates {@link Submission}
   *
   * @param submission submission
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return created {@link Submission}
   */
  CompletableFuture<Submission> createSubmission(Submission submission, Context context, Map<String, String> headers);

  /**
   * This method returns {@link Submission} by ID
   *
   * @param id      submission's id
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return {@link Submission}
   */
  CompletableFuture<Submission> getSubmissionById(String id, Context context, Map<String, String> headers);

  /**
   * This method returns {@link Submissions}
   *
   * @param offset  offset
   * @param limit   limit
   * @param lang    language
   * @param query   query
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return collection of submissions{@link Submissions}
   */
  CompletableFuture<Submissions> getSubmissions(int offset, int limit, String lang, String query, Context context, Map<String, String> headers);

  /**
   * This method updates {@link Submission} by ID
   *
   * @param id                  updated submission's id
   * @param updatedSubmission   updated submission object
   * @param context             Vert.X context
   * @param headers             OKAPI headers
   * @return {@link Submission}
   */
  CompletableFuture<Void> updateSubmissionById(String id, Submission updatedSubmission, Context context, Map<String, String> headers);

  /**
   * This method deletes {@link Submission} by ID
   *
   * @param id        updated submission's id
   * @param context   Vert.X context
   * @param headers   OKAPI headers
   * @return void
   */
  CompletableFuture<Void> deleteSubmissionById(String id, Context context, Map<String, String> headers);

  /**
   * This method returns {@link Requests} associated with a given submission
   *
   * @param id    submission ID
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return {@link Requests}
   */
  CompletableFuture<Requests> getSubmissionRequestsById(String id, Context context, Map<String, String> headers);
}
