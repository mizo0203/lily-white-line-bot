package com.mizo0203.lilywhite.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class Translator {

  private final DateFormat mDateFormat;

  Translator() {
    mDateFormat = new SimpleDateFormat(Define.DATE_FORMAT_PATTERN);
    mDateFormat.setTimeZone(Define.LINE_TIME_ZONE);
  }

  public String formatDate(Date date) {
    return mDateFormat.format(date) + " " + Define.DATE_JST;
  }
}
