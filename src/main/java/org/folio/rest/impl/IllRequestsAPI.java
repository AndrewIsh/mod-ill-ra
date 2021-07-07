package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;
import static org.folio.config.Constants.OKAPI_URL;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import org.folio.rest.jaxrs.model.Request;
import org.folio.rest.jaxrs.model.Status;
import org.folio.rest.jaxrs.resource.IllRa;
import org.folio.service.illrequest.IllrequestService;
import org.folio.service.illstatus.IllstatusService;
import org.folio.spring.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;
import java.util.Map;

public class IllRequestsAPI extends BaseApi implements IllRa {

  private static final String REQUESTS_LOCATION_PREFIX = "/requests/%s";
  private static final String STATUSES_LOCATION_PREFIX = "/statuses/%s";

  @Autowired
  private IllrequestService illrequestService;
  @Autowired
  private IllstatusService illstatusService;

  public IllRequestsAPI() {
    SpringContextUtil.autowireDependencies(this, Vertx.currentContext());
  }

  @Override
  public void getIllRaRequests(int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illrequestService.getRequests(offset, limit, lang, vertxContext, okapiHeaders)
      .thenAccept(requests -> asyncResultHandler.handle(succeededFuture(buildOkResponse(requests))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void postIllRaRequests(String lang, Request request, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illrequestService.createRequest(request, vertxContext, okapiHeaders)
      .thenAccept(req -> asyncResultHandler.handle(succeededFuture(buildResponseWithLocation(okapiHeaders.get(OKAPI_URL),
        String.format(REQUESTS_LOCATION_PREFIX, req.getId()), request))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void getIllRaRequestsByRequestId(String requestId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illrequestService.getRequestById(requestId, vertxContext, okapiHeaders)
      .thenAccept(request -> asyncResultHandler.handle(succeededFuture(buildOkResponse(request))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void putIllRaRequestsByRequestId(String requestId, String lang, Request request, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illrequestService.updateRequestById(requestId, request, vertxContext, okapiHeaders)
      .thenAccept(vVoid -> asyncResultHandler.handle(succeededFuture(buildNoContentResponse())))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void deleteIllRaRequestsByRequestId(String requestId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
  }

  @Override
  public void postIllRaConnectorByConnectorId(String connectorId, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

  }

  @Override
  public void deleteIllRaConnectorByConnectorId(String connectorId, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

  }

  @Override
  public void getIllRaStatuses(int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illstatusService.getStatuses(offset, limit, lang, vertxContext, okapiHeaders)
      .thenAccept(statuses -> asyncResultHandler.handle(succeededFuture(buildOkResponse(statuses))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void postIllRaStatuses(String lang, Status status, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illstatusService.createStatus(status, vertxContext, okapiHeaders)
      .thenAccept(stat -> asyncResultHandler.handle(succeededFuture(buildResponseWithLocation(okapiHeaders.get(OKAPI_URL),
        String.format(STATUSES_LOCATION_PREFIX, stat.getId()), status))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void getIllRaStatusesByStatusId(String statusId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illstatusService.getStatusById(statusId, vertxContext, okapiHeaders)
      .thenAccept(status -> asyncResultHandler.handle(succeededFuture(buildOkResponse(status))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void putIllRaStatusesByStatusId(String statusId, String lang, Status status, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illstatusService.updateStatusById(statusId, status, vertxContext, okapiHeaders)
      .thenAccept(vVoid -> asyncResultHandler.handle(succeededFuture(buildNoContentResponse())))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void deleteIllRaStatusesByStatusId(String statusId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illstatusService.deleteStatusById(statusId, vertxContext, okapiHeaders)
      .thenAccept(vVoid -> asyncResultHandler.handle(succeededFuture(buildNoContentResponse())))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }
}
