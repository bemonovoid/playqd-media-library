package io.playqd.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Formatter;
import java.util.Locale;

public final class TimeUtils {

  private static final String DLNA_DURATION_FORMAT = "%01d:%02d:%06.3f";

  private TimeUtils() {

  }

  public static String durationToTimeFormat(Duration duration) {
    var hours = duration.toHours();
    if (hours > 0) {
      return String.format("%s:%s:%s",
          hours, unitPartToTimeFormat(duration.toMinutesPart()), unitPartToTimeFormat(duration.toSecondsPart()));
    }
    var durationInMinutes = duration.toMinutes();
    if (durationInMinutes > 0) {
      return String.format("%s:%s",
          unitPartToTimeFormat(duration.toMinutesPart()), unitPartToTimeFormat(duration.toSecondsPart()));
    }
    var durationInSeconds = duration.toSeconds();
    if (durationInSeconds > 0) {
      return String.format("00:%s", unitPartToTimeFormat(duration.toSecondsPart()));
    }
    var durationInMillis = duration.toMillis();
    if (durationInMillis > 0) {
      return String.format("00.%s", durationInMillis);
    }
    return "00:00";
  }

  public static String durationToDisplayString(Duration duration) {
    var hours = duration.toHours();
    if (hours > 0) {
      return hours + " hour(s) and " + duration.toMinutesPart() + " minute(s)";
    }
    var durationInMinutes = duration.toMinutes();
    if (durationInMinutes > 0) {
      return durationInMinutes + " minutes(s) and " + duration.toSecondsPart() + " second(s)";
    }
    var durationInSeconds = duration.toSeconds();
    if (durationInSeconds > 0) {
      return durationInSeconds + " second(s)";
    }
    var durationInMillis = duration.toMillis();
    if (durationInMillis > 0) {
      return durationInMillis + " milliseconds";
    }
    return "Ohh, that was blasting fast!";
  }

  public static String durationToDlnaFormat(double duration) {
    double seconds;
    int hours;
    int minutes;
    if (duration < 0) {
      seconds = 0.0;
      hours = 0;
      minutes = 0;
    } else {
      seconds = duration % 60;
      hours = (int) (duration / 3600);
      minutes = ((int) (duration / 60)) % 60;
    }
    if (hours > 99999) {
      // As per DLNA standard
      hours = 99999;
    }
    StringBuilder sb = new StringBuilder();
    try (Formatter formatter = new Formatter(sb, Locale.ROOT)) {
      formatter.format(DLNA_DURATION_FORMAT, hours, minutes, seconds);
    }
    return sb.toString();
  }

  public static Instant millisToInstant(long millis) {
    return Instant.ofEpochMilli(millis);
  }

  public static LocalDate millisToLocalDate(long millis) {
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  private static String unitPartToTimeFormat(int timeUnitPart) {
    if (timeUnitPart < 10) {
      return "0" + timeUnitPart;
    }
    return "" + timeUnitPart;
  }
}
