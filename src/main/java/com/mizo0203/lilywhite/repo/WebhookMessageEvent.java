package com.mizo0203.lilywhite.repo;

import com.linecorp.bot.model.event.message.MessageContent;

public interface WebhookMessageEvent<T extends MessageContent> {
  void callback(T message);
}
