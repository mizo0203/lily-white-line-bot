package com.mizo0203.lilywhite.repo.line.messaging.data.action;

public abstract class Action {

  public final String type;

  public Action(String type) {
    this.type = type;
  }
}
