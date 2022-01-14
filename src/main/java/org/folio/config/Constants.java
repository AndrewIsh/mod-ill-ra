package org.folio.config;

import java.util.ArrayList;
import java.util.Arrays;

public class Constants {

  private Constants() {
  }

  public static final String ID = "id";
  public static final String OKAPI_URL = "x-okapi-url";
  public static final int CONNECTOR_CONNECT_TIMEOUT = 5;
  public static final int CONNECTOR_RESPONSE_TIMEOUT = 10;
  public static final String ISO18626_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

  public static final String STORAGE_SERVICE = "/ill-ra-storage/";

  public static final ArrayList<String> CONNECTOR_ABILITIES = new ArrayList<>(
    Arrays.asList("ill-connector-search", "ill-connector-action", "ill-connector-sa-update")
  );
}
