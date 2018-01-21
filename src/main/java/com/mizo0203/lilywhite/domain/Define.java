package com.mizo0203.lilywhite.domain;

import java.util.TimeZone;

public class Define {
  /** LINE アプリから日時指定操作をする場合は日本標準時(JST)で指定すること */
  public static final TimeZone LINE_TIME_ZONE = TimeZone.getTimeZone("Asia/Tokyo");

  public static final String DATE_FORMAT_PATTERN = "M/d(E) H:mm";

  public static final String DATE_JST = "(JST)";

  public enum Mode {
    DATE,
    TIME,
    DATE_TIME,
    ;

    @Override
    public String toString() {
      switch (this) {
        case DATE:
          return "date";
        case TIME:
          return "time";
        case DATE_TIME:
          return "datetime";
        default:
          throw new IllegalStateException(super.toString());
      }
    }
  }
}
