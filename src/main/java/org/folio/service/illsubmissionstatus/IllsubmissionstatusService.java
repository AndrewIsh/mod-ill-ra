package org.folio.service.illsubmissionstatus;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.jaxrs.model.SubmissionStatus;
import org.folio.rest.jaxrs.model.SubmissionStatuses;

import io.vertx.core.Context;

public interface IllsubmissionstatusService {

  /**
   * This method creates {@link SubmissionStatus}
   *
   * @param status {@link SubmissionStatus}
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return created {@link SubmissionStatus}
   */
  CompletableFuture<SubmissionStatus> createSubmissionStatus(SubmissionStatus status, Context context, Map<String, String> headers);

  /**
   * This method returns {@link SubmissionStatus} by ID
   *
   * @param id      status' id
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return {@link SubmissionStatus}
   */
  CompletableFuture<SubmissionStatus> getSubmissionStatusById(String id, Context context, Map<String, String> headers);

  /**
   * This method returns {@link SubmissionStatuses}
   *
   * @param offset  offset
   * @param limit   limit
   * @param lang    language
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return collection of submission statuses {@link SubmissionStatuses}
   */
  CompletableFuture<SubmissionStatuses> getSubmissionStatuses(int offset, int limit, String lang, Context context, Map<String, String> headers);

  /**
   * This method updates {@link SubmissionStatus} by ID
   *
   * @param id                  updated status' id
   * @param updatedStatus       updated status object
   * @param context             Vert.X context
   * @param headers             OKAPI headers
   * @return void
   */
  CompletableFuture<Void> updateSubmissionStatusById(String id, SubmissionStatus updatedStatus, Context context, Map<String, String> headers);

  /**
   * This method deletes {@link SubmissionStatus} by ID
   *
   * @param id                  updated status' id
   * @param context             Vert.X context
   * @param headers             OKAPI headers
   * @return void completable future
   */
  CompletableFuture<Void> deleteSubmissionStatusById(String id, Context context, Map<String, String> headers);
}
