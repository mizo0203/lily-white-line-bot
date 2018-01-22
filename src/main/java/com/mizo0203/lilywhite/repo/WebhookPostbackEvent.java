package com.mizo0203.lilywhite.repo;

public interface WebhookPostbackEvent<T> {
  void callback(T param);
}
