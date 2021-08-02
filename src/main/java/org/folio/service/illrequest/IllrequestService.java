package org.folio.service.illrequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.jaxrs.model.Request;
import org.folio.rest.jaxrs.model.Requests;

import io.vertx.core.Context;

public interface IllrequestService {

  /**
   * This method creates {@link Request}
   *
   * @param request request
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return created {@link Request}
   */
  CompletableFuture<Request> createRequest(Request request, Context context, Map<String, String> headers);

  /**
   * This method returns {@link Request} by ID
   *
   * @param id      request's id
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return {@link Request}
   */
  CompletableFuture<Request> getRequestById(String id, Context context, Map<String, String> headers);

  /**
   * This method returns {@link Requests}
   *
   * @param offset  offset
   * @param limit   limit
   * @param query   query
   * @param lang    language
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return collection of requests{@link Requests}
   */
  CompletableFuture<Requests> getRequests(int offset, int limit, String lang, String query, Context context, Map<String, String> headers);

  /**
   * This method updates {@link Request} by ID
   *
   * @param id                  updated request's id
   * @param updatedRequest      updated request object
   * @param context             Vert.X context
   * @param headers             OKAPI headers
   * @return {@link Request}
   */
  CompletableFuture<Void> updateRequestById(String id, Request updatedRequest, Context context, Map<String, String> headers);

  /**
   * This method deletes {@link Request} by ID
   *
   * @param id                  deleted request's id
   * @param context             Vert.X context
   * @param headers             OKAPI headers
   * @return void
   */
  CompletableFuture<Void> deleteRequestById(String id, Context context, Map<String, String> headers);
}
