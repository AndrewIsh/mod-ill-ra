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

public class IllConnectorService extends BaseService {

  // Return a CompletableFuture containing a list of available connectors supporting a
  // given interface
  public CompletableFuture<ArrayList<String>> getConnectorsSupporting(String supporting, Map<String, String> headers) {
    OkapiParams okapiParams = new OkapiParams(headers);
    HttpClient client = HttpClient.newBuilder().build();

    String endpoint = okapiParams.getUrl() + "/_/proxy/tenants/" + okapiParams.getTenant() + "/modules?provide=" + supporting;
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(endpoint))
      .GET()
      .build();

    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
      .thenApply(response -> {
        ArrayList<String> modules = new ArrayList<>();
        JsonArray jsonArray = new JsonArray(response.body());
        for(int i=0; i < jsonArray.size(); i++ ) {
          JsonObject obj = jsonArray.getJsonObject(i);
          modules.add(obj.getString("id"));
        }
        return modules;
      });
  }
}
