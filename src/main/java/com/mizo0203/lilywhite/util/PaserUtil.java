package com.mizo0203.lilywhite.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.mizo0203.lilywhite.repo.line.messaging.data.webHook.event.RequestBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class PaserUtil {

  /** @see Gson#fromJson(String, Class) */
  public static RequestBody parseWebhooksData(String json) {
    return new Gson().fromJson(json, RequestBody.class);
  }

  /** @see ObjectMapper#writeValueAsString(Object) */
  public static String toJson(ReplyMessage data) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(data);
  }

  /** @see ObjectMapper#writeValueAsString(Object) */
  public static String toJson(PushMessage data) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(data);
  }

  private static String parseString(BufferedReader br) throws IOException {
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    br.close();
    return sb.toString();
  }

  public static String parseString(InputStream is) throws IOException {
    InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
    return parseString(new BufferedReader(isr));
  }
}
