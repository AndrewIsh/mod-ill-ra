package org.folio.service.illsupplier;

import io.vertx.core.Context;
import org.folio.rest.jaxrs.model.Suppliers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IllsupplierService {

  /**
   * This method returns {@link Suppliers}
   *
   * @param offset  offset
   * @param limit   limit
   * @param lang    language
   * @param context Vert.X context
   * @param headers OKAPI headers
   * @return collection of suppliers{@link Suppliers}
   */
  CompletableFuture<Suppliers> getSuppliers(int offset, int limit, String lang, Context context, Map<String, String> headers);
}
