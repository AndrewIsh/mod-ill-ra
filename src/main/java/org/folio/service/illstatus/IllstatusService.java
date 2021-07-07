package org.folio.service.illstatus;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.jaxrs.model.Status;
import org.folio.rest.jaxrs.model.Statuses;

import io.vertx.core.Context;

public interface IllstatusService {

  /**
   * This method creates {@link Status}
   *
   * @param status status
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return created {@link Status}
   */
  CompletableFuture<Status> createStatus(Status status, Context context, Map<String, String> headers);

  /**
   * This method returns {@link Status} by ID
   *
   * @param id      status' id
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return {@link Status}
   */
  CompletableFuture<Status> getStatusById(String id, Context context, Map<String, String> headers);

  /**
   * This method returns {@link Statuses}
   *
   * @param offset  offset
   * @param limit   limit
   * @param lang    language
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return collection of statuses{@link Statuses}
   */
  CompletableFuture<Statuses> getStatuses(int offset, int limit, String lang, Context context, Map<String, String> headers);

  /**
   * This method updates {@link Status} by ID
   *
   * @param id                  updated status' id
   * @param updatedStatus       updated status object
   * @param context             Vert.X context
   * @param headers             OKAPI headers
   * @return {@link Status}
   */
  CompletableFuture<Void> updateStatusById(String id, Status updatedStatus, Context context, Map<String, String> headers);

  /**
   * This method deletes {@link Status} by ID
   *
   * @param id                  updated status' id
   * @param context             Vert.X context
   * @param headers             OKAPI headers
   * @return void completable future
   */
  CompletableFuture<Void> deleteStatusById(String id, Context context, Map<String, String> headers);
}
