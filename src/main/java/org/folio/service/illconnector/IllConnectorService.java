package org.folio.service.illconnector;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.folio.service.BaseService;
import org.folio.common.OkapiParams;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.folio.config.Constants.*;

public class IllConnectorService extends BaseService {

  // Return a CompletableFuture containing a list of available connectors
  public CompletableFuture<Object> getConnectorsSupporting(Map<String, String> headers) {
    // First get the tenant as we need to interpolate that into the endpoint
    OkapiParams okapiParams = new OkapiParams(headers);
    HttpClient client = HttpClient.newBuilder().build();

    ArrayList<CompletableFuture<HttpResponse<String>>> completableFutures = new ArrayList<>();

    for(String ability: CONNECTOR_ABILITIES) {
      String endpoint = okapiParams.getUrl() + "/_/proxy/tenants/" + okapiParams.getTenant() + "/modules?provide=" + ability;
      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(endpoint))
        .GET()
        .build();
      completableFutures.add(
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      );
    }

    // Build a hashmap, key is module ID, value is 1
    // This enables us to dedupe as we go
    // FIXME: Although this works, the syntax is a bit screwy. We're trying to build
    // a de-duped list of module IDs from a stream of API responses, each response can
    // contain a list of IDs. I'm struggling to wrap my head around how we can reduce
    // that down into a single list. As it is, we're building an object outside of the
    // stream processing
    HashMap<String, Integer> modules = new HashMap<>();
    return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
      .thenApply(__ -> completableFutures.stream()
        .map(CompletableFuture::join)
        // Each stream element is a response containing an array of service IDs
        // e.g. [ { "id": "mod-ill-connector-bldss-1.0" } ]
        .map(response -> {
          // We receive a JSON array containing objects, each object has a
          // single id property, we want to create an array of the values of
          // id
          JsonArray jsonArray = new JsonArray(response.body());
          for(int i=0; i < jsonArray.size(); i++ ) {
            JsonObject obj = jsonArray.getJsonObject(i);
            modules.put(obj.getString("id"), 1);
          }
          return modules;
        })
        .collect(Collectors.toList()))
        .thenApply(__ -> modules.keySet());
  }
}
