package org.folio.util;

/*
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateTimeUtil {
  private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  public String formatDateTime(DateTime dateTime) {
    return dateTime.toString(formatter);
  }

  public Date getTimestamp() {
    DateTime now = DateTime.now();
    return formatDateTime(now);
  }
}
*/

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtil {
  private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneOffset.UTC);

  public String getTimestamp() {
    return dateTimeFormatter.format(ZonedDateTime.now());
  }

  public Date getTimestampDate() {
    return new Date(getTimestamp());
  }
}
