package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;
import static org.folio.config.Constants.*;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import io.vertx.core.json.JsonObject;
import org.folio.domain.SubmittableRequest;
import org.folio.domain.SubmittableSubmission;
import org.folio.domain.SupplyingAgency;
import org.folio.rest.jaxrs.model.*;
import org.folio.rest.jaxrs.model.supplying_agency_message_storage.request.SupplyingAgencyMessageStorageRequest;
import org.folio.rest.jaxrs.resource.IllRa;
import org.folio.service.illconnector.IllConnectorService;
import org.folio.service.illsubmission.IllsubmissionService;
import org.folio.service.illrequest.IllrequestService;
import org.folio.service.illsubmissionstatus.IllsubmissionstatusService;
import org.folio.service.illsupplingagency.IllSupplyingAgencyService;
import org.folio.spring.SpringContextUtil;
import org.folio.util.DateTimeUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class IllRequestsAPI extends BaseApi implements IllRa {

  private static final String SUBMISSIONS_LOCATION_PREFIX = "/submissions/%s";
  private static final String REQUESTS_LOCATION_PREFIX = "/requests/%s";
  private static final String SUBMISSION_STATUSES_LOCATION_PREFIX = "/submission-statuses/%s";

  @Autowired
  private IllsubmissionService illsubmissionService;
  @Autowired
  private IllrequestService illrequestService;
  @Autowired
  private IllsubmissionstatusService illsubmissionstatusService;

  public IllRequestsAPI() {
    SpringContextUtil.autowireDependencies(this, Vertx.currentContext());
  }

  private CompletableFuture<Submission> submitSubmission(Submission submission, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    return illsubmissionService.createSubmission(submission, vertxContext, okapiHeaders);
  }

  private CompletableFuture<Request> submitRequest(Request request, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    return illrequestService.createRequest(request, vertxContext, okapiHeaders);
  }

  private CompletableFuture<SaRequestResponse> submitToSupplier(SaRequestRequest request, Context vertxContext, Map<String, String> okapiHeaders) {
    IllSupplyingAgencyService sa = new IllSupplyingAgencyService();
    SupplyingAgency saUtil = new SupplyingAgency();
    JsonObject requestBody = saUtil.buildRequest(request);
    return sa.sendSupplierRequest(requestBody, vertxContext, okapiHeaders);
  }

  @Override
  public void getIllRaConnectorsBySupporting(String supporting, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    // Get all connectors supporting the "info" interface, i.e. all available connectors
    IllConnectorService ics = new IllConnectorService();
    ics.getConnectorsSupporting(supporting, okapiHeaders)
      .thenAccept(res -> {
        // We're using org.json JSONObject instead of Vert.x JsonObject because
        // for reasons that I couldn't fathom, the latter was creating "map" and
        // "empty" objects in the new JsonObject and I was not able to remove them
        JSONObject response = new JSONObject();
        response.put("connectors", res);
        asyncResultHandler.handle(succeededFuture(buildOkResponse(response.toString())));
      });

  }

  @Override
  public void getIllRaConnectors(Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    // Get all connectors supporting the "info" interface, i.e. all available connectors
    getIllRaConnectorsBySupporting("ill-connector-info", okapiHeaders, asyncResultHandler, vertxContext);
  }

  @Override
  public void getIllRaSearch(int offset, int limit, String query, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    IllSupplyingAgencyService sa = new IllSupplyingAgencyService();
    sa.sendSearch(query, okapiHeaders)
      .thenAccept(response -> asyncResultHandler.handle(succeededFuture(buildOkResponse(response))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  // Some pretty gnarly chaining going on here. First we create a submission, then
  // use the created submission to create a request, then use that to make a request
  // with the supplier
  public void postIllRaSaRequest(SaRequestRequest request, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    Submission submission = new SubmittableSubmission(request).build();
    submitSubmission(submission, okapiHeaders, asyncResultHandler, vertxContext)
      .thenApply(createdSub -> new SubmittableRequest(createdSub, request).build())
      .thenApply(submittableRequest -> submitRequest(submittableRequest, okapiHeaders, asyncResultHandler, vertxContext))
      .thenApply(submittedRequest -> submitToSupplier(request, vertxContext, okapiHeaders))
      .thenAccept(supplierResponse -> {
        supplierResponse.thenAccept(sr -> {
            asyncResultHandler.handle(succeededFuture(buildOkResponse(sr)));
        })
        .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
      })
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void getIllRaSubmissions(int offset, int limit, String lang, String query, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illsubmissionService.getSubmissions(offset, limit, lang, query, vertxContext, okapiHeaders)
      .thenAccept(submissions -> asyncResultHandler.handle(succeededFuture(buildOkResponse(submissions))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void postIllRaSubmissions(String lang, Submission entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    // Populate the date this submission was made
    entity.setSubmissionDate(DateTimeUtils.dtToString(ZonedDateTime.now(), ISO18626_DATE_FORMAT));
    // TODO: This wil be whatever is set as the "initial" status
    entity.setStatusId("49600f35-e2af-45ef-a03c-ee0de7ec3c89");
    illsubmissionService.createSubmission(entity, vertxContext, okapiHeaders)
      .thenAccept(sub -> asyncResultHandler.handle(succeededFuture(buildResponseWithLocation(okapiHeaders.get(OKAPI_URL),
        String.format(SUBMISSIONS_LOCATION_PREFIX, sub.getId()), entity))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void getIllRaSubmissionsBySubmissionId(String submissionId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illsubmissionService.getSubmissionById(submissionId, vertxContext, okapiHeaders)
      .thenAccept(submission -> asyncResultHandler.handle(succeededFuture(buildOkResponse(submission))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void putIllRaSubmissionsBySubmissionId(String submissionId, String lang, Submission entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illsubmissionService.updateSubmissionById(submissionId, entity, vertxContext, okapiHeaders)
      .thenAccept(vVoid -> asyncResultHandler.handle(succeededFuture(buildNoContentResponse())))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));

  }

  @Override
  public void deleteIllRaSubmissionsBySubmissionId(String submissionId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illsubmissionService.deleteSubmissionById(submissionId, vertxContext, okapiHeaders)
      .thenAccept(vVoid -> asyncResultHandler.handle(succeededFuture(buildNoContentResponse())))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void getIllRaSubmissionsRequestsBySubmissionId(String submissionId, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illsubmissionService.getSubmissionRequestsById(submissionId, vertxContext, okapiHeaders)
      .thenAccept(requests -> asyncResultHandler.handle(succeededFuture(buildOkResponse(requests))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void postIllRaSubmissionsRequestsBySubmissionId(String submissionId, String lang, Request entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

  }

  @Override
  public void getIllRaRequests(int offset, int limit, String lang, String query, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illrequestService.getRequests(offset, limit, lang, query, vertxContext, okapiHeaders)
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
    illrequestService.deleteRequestById(requestId, vertxContext, okapiHeaders)
      .thenAccept(vVoid -> asyncResultHandler.handle(succeededFuture(buildNoContentResponse())))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void getIllRaSubmissionStatuses(int offset, int limit, String query, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illsubmissionstatusService.getSubmissionStatuses(offset, limit, lang, vertxContext, okapiHeaders)
      .thenAccept(submissionStatuses -> asyncResultHandler.handle(succeededFuture(buildOkResponse(submissionStatuses))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void postIllRaSubmissionStatuses(String lang, SubmissionStatus entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illsubmissionstatusService.createSubmissionStatus(entity, vertxContext, okapiHeaders)
      .thenAccept(submissionStatus -> asyncResultHandler.handle(succeededFuture(buildResponseWithLocation(okapiHeaders.get(OKAPI_URL),
        String.format(SUBMISSION_STATUSES_LOCATION_PREFIX, submissionStatus.getId()), entity))))
      .exceptionally(t ->handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void getIllRaSubmissionStatusesByStatusId(String statusId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illsubmissionstatusService.getSubmissionStatusById(statusId, vertxContext, okapiHeaders)
      .thenAccept(submissionStatus -> asyncResultHandler.handle(succeededFuture(buildOkResponse(submissionStatus))))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void putIllRaSubmissionStatusesByStatusId(String statusId, String lang, SubmissionStatus entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illsubmissionstatusService.updateSubmissionStatusById(statusId, entity, vertxContext, okapiHeaders)
      .thenAccept(vVoid -> asyncResultHandler.handle(succeededFuture(buildNoContentResponse())))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void deleteIllRaSubmissionStatusesByStatusId(String statusId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    illsubmissionstatusService.deleteSubmissionStatusById(statusId, vertxContext, okapiHeaders)
      .thenAccept(vVoid -> asyncResultHandler.handle(succeededFuture(buildNoContentResponse())))
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));
  }

  @Override
  public void postIllRaSaUpdate(SaMessageRequest entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    // We've received an update from a supplier, act on it

    // First store the message
    String requestId = entity.getHeader().getRequestingAgencyRequestId();
    // We store the entire message
    String message = JsonObject.mapFrom(entity).toString();
    SupplyingAgencyMessageStorageRequest samsr = new SupplyingAgencyMessageStorageRequest()
      .withRequestId(requestId)
      .withMessage(message);
    IllSupplyingAgencyService isas = new IllSupplyingAgencyService();
    isas.storeSupplierMessage(samsr, requestId, vertxContext, okapiHeaders)
      .thenAccept(vVoid -> {
        // If we managed to store the message, we can construct and return
        // a confirmation
        //
        // Gather what we need for the confirmation
        SupplyingAgencyMessageHeader header = entity.getHeader();
        SupplyingAgencyMessageInfo info = entity.getMessageInfo();
        String reasonForMessage = info.getReasonForMessage().toString();
        String now = DateTimeUtils.dtToString(ZonedDateTime.now(), ISO18626_DATE_FORMAT);

        SupplyingAgencyConfirmationHeader.ReasonForMessage responseReason = SupplyingAgencyConfirmationHeader.ReasonForMessage.fromValue(reasonForMessage);

        SupplyingAgencyConfirmationHeader supplyingAgencyConfirmationHeader = new SupplyingAgencyConfirmationHeader()
          .withSupplyingAgencyId(header.getSupplyingAgencyId())
          .withRequestingAgencyId(header.getRequestingAgencyId())
          .withTimestamp(now)
          .withRequestingAgencyRequestId(header.getRequestingAgencyRequestId())
          .withTimestampReceived(now)
          .withMessageStatus(SupplyingAgencyConfirmationHeader.MessageStatus.OK)
          .withReasonForMessage(responseReason);
        SaMessageResponse response = new SaMessageResponse()
          .withHeader(supplyingAgencyConfirmationHeader);

        asyncResultHandler.handle(succeededFuture(buildOkResponse(response)));
      })
      .exceptionally(t -> handleErrorResponse(asyncResultHandler, t));

  }
/*
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
  public void postIllRaStatuses(String lang,  status, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
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
  */
}
