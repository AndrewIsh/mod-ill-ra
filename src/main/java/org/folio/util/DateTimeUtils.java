package org.folio.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

  public static ZonedDateTime stringToDT(String input) {
    return ZonedDateTime.parse(input);
  }

  public static String dtToString(ZonedDateTime input, String pattern) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return input.format(formatter);
  }
}
