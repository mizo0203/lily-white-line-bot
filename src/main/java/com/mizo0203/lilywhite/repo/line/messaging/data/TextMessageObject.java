package com.mizo0203.lilywhite.repo.line.messaging.data;

@SuppressWarnings({"unused", "WeakerAccess"})
public class TextMessageObject extends MessageObject {

  public final String text;

  public TextMessageObject(String text) {
    super("text");
    this.text = text;
  }
}
