package com.mizo0203.lilywhite.domain;

import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.DatetimePickerAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.*;
import com.linecorp.bot.model.event.message.*;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.message.template.Template;
import com.mizo0203.lilywhite.repo.*;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class UseCase {
  public static final String ACTION_DATA_REQUEST_REMINDER_DATE_SET =
      "ACTION_DATA_REQUEST_REMINDER_DATE_SET";
  public static final String ACTION_DATA_REQUEST_REMINDER_CANCELLATION =
      "ACTION_DATA_REQUEST_REMINDER_CANCELLATION";
  public static final String ACTION_DATA_CANCEL_REMINDER = "ACTION_DATA_CANCEL_REMINDER";
  public static final String ACTION_DATA_NOT_CANCEL_REMINDER = "ACTION_DATA_NOT_CANCEL_REMINDER";
  public static final String ACTION_DATA_REQUEST_RESET = "ACTION_DATA_REQUEST_RESET";
  private final Repository mRepository;
  private final Translator mTranslator;

  public UseCase() {
    mRepository = new Repository();
    mTranslator = new Translator();
  }

  public void destroy() {
    mRepository.destroy();
  }

  public void initSource(String source_id) {
    mRepository.clearEvent(source_id);
  }

  /** リマインダーメッセージを送信する */
  public void pushReminderMessage(String source_id, String message) {
    mRepository.pushMessage(
        source_id, new TextMessage(message), createTemplateMessageToReset("リマインダーをリセットしますよー"));
  }

  private Message createTemplateMessageToReset(String text) {
    return new TemplateMessage(
        "テンプレートメッセージはiOS版およびAndroid版のLINE 6.7.0以降で対応しています。", createTemplateToReset(text));
  }

  private Template createTemplateToReset(String text) {
    return new ButtonsTemplate(null, null, text, createPostBackActionsToRequestReset());
  }

  private List<Action> createPostBackActionsToRequestReset() {
    return Collections.singletonList(new PostbackAction("リセット", ACTION_DATA_REQUEST_RESET));
  }

  public void replyMessageToRequestReminderMessage(String replyToken) {
    mRepository.replyMessage(
        replyToken, new TextMessage("リマインダーをセットしますよー\nメッセージを返信してくださーい\n例) 春ですよー"));
  }

  public void replyMessageToRequestReminderDate(String replyToken) {
    mRepository.replyMessage(replyToken, createMessageToRequestReminderDate());
  }

  private Message createMessageToRequestReminderDate() {
    return new TemplateMessage(
        "テンプレートメッセージはiOS版およびAndroid版のLINE 6.7.0以降で対応しています。",
        createButtonsTemplateToRequestReminderDate());
  }

  private Template createButtonsTemplateToRequestReminderDate() {
    return ButtonsTemplate.builder()
        .text("リマインダー日時をセットしますよー")
        .actions(createDateTimePickerActionsToRequestReminderDate())
        .build();
  }

  private List<Action> createDateTimePickerActionsToRequestReminderDate() {
    return Collections.singletonList(createDateTimePickerActionToRequestReminderDate());
  }

  private Action createDateTimePickerActionToRequestReminderDate() {
    return new DatetimePickerAction(
        "セット", ACTION_DATA_REQUEST_REMINDER_DATE_SET, Define.Mode.DATE_TIME.toString());
  }

  public void enqueueReminderTask(String source_id, Date date) {
    mRepository.enqueueReminderTask(source_id, date.getTime());
  }

  public State getState(String source_id) {
    return mRepository.getState(source_id);
  }

  public void setReminderMessage(String sourceId, String reminderMessage) {
    mRepository.setReminderMessage(sourceId, reminderMessage);
  }

  public void replyReminderConfirmMessage(String replyToken, Date date) {
    mRepository.replyMessage(
        replyToken,
        createMessageToConfirmReminder("リマインダーをセットしましたよー\n" + mTranslator.formatDate(date)));
  }

  private Message createMessageToConfirmReminder(String text) {
    return new TemplateMessage(
        "テンプレートメッセージはiOS版およびAndroid版のLINE 6.7.0以降で対応しています。",
        createButtonsTemplateToConfirmReminder(text));
  }

  private Template createButtonsTemplateToConfirmReminder(String text) {
    return ButtonsTemplate.builder()
        .text(text)
        .actions(createPostbackActionsToRequestReminderCancellation())
        .build();
  }

  private List<Action> createPostbackActionsToRequestReminderCancellation() {
    return Collections.singletonList(createPostbackActionToRequestReminderCancellation());
  }

  private Action createPostbackActionToRequestReminderCancellation() {
    return new PostbackAction("キャンセル", ACTION_DATA_REQUEST_REMINDER_CANCELLATION);
  }

  public void replyReminderCancellationConfirmMessage(String replyToken) {
    mRepository.replyMessage(replyToken, createMessageToConfirmCancellationReminder());
  }

  private Message createMessageToConfirmCancellationReminder() {
    return new TemplateMessage(
        "テンプレートメッセージはiOS版およびAndroid版のLINE 6.7.0以降で対応しています。",
        createConfirmTemplateToConfirmCancellationReminder());
  }

  private Template createConfirmTemplateToConfirmCancellationReminder() {
    return new ConfirmTemplate(
        "本当にキャンセルしますかー？", createPostbackActionsToConfirmCancellationReminder());
  }

  private List<Action> createPostbackActionsToConfirmCancellationReminder() {
    return Arrays.asList(
        createPostbackActionToCancelReminder(), createPostbackActionToNotCancelReminder());
  }

  private Action createPostbackActionToCancelReminder() {
    return new PostbackAction("はい", ACTION_DATA_CANCEL_REMINDER);
  }

  private Action createPostbackActionToNotCancelReminder() {
    return new PostbackAction("いいえ", ACTION_DATA_NOT_CANCEL_REMINDER);
  }

  public void replyCanceledReminderMessage(String replyToken) {
    mRepository.replyMessage(replyToken, createTemplateMessageToReset("キャンセルしましたー"));
  }

  public void replyNotCanceledReminderMessage(String replyToken) {
    mRepository.replyMessage(replyToken, new TextMessage("そのままですー"));
  }

  public void setCancellationConfirm(String sourceId, boolean cancellationConfirm) {
    mRepository.setCancellationConfirm(sourceId, cancellationConfirm);
  }

  public void parseWebhookEvent(
      HttpServletRequest req,
      @Nullable WebhookEvent<MessageEvent> onMessageEvent,
      @Nullable WebhookEvent<UnfollowEvent> onUnfollowEvent,
      @Nullable WebhookEvent<FollowEvent> onFollowEvent,
      @Nullable WebhookEvent<JoinEvent> onJoinEvent,
      @Nullable WebhookEvent<LeaveEvent> onLeaveEvent,
      @Nullable WebhookEvent<PostbackEvent> onPostbackEvent,
      @Nullable WebhookEvent<BeaconEvent> onBeaconEvent) {
    List<Event> eventList = getCallbackEventList(req);
    if (eventList == null) {
      return;
    }
    for (Event event : eventList) {
      if (event instanceof MessageEvent) {
        if (onMessageEvent != null) {
          onMessageEvent.callback((MessageEvent) event);
        }
      } else if (event instanceof UnfollowEvent) {
        if (onUnfollowEvent != null) {
          onUnfollowEvent.callback((UnfollowEvent) event);
        }
      } else if (event instanceof FollowEvent) {
        if (onFollowEvent != null) {
          onFollowEvent.callback((FollowEvent) event);
        }
      } else if (event instanceof JoinEvent) {
        if (onJoinEvent != null) {
          onJoinEvent.callback((JoinEvent) event);
        }
      } else if (event instanceof LeaveEvent) {
        if (onLeaveEvent != null) {
          onLeaveEvent.callback((LeaveEvent) event);
        }
      } else if (event instanceof PostbackEvent) {
        if (onPostbackEvent != null) {
          onPostbackEvent.callback((PostbackEvent) event);
        }
      } else if (event instanceof BeaconEvent) {
        if (onBeaconEvent != null) {
          onBeaconEvent.callback((BeaconEvent) event);
        }
      }
    }
  }

  @Nullable
  private List<Event> getCallbackEventList(HttpServletRequest req) {
    return mRepository.getCallbackEventList(req);
  }

  public void parseMessageEvent(
      MessageContent message,
      @Nullable WebhookMessageEvent<TextMessageContent> onTextMessageEvent,
      @Nullable WebhookMessageEvent<ImageMessageContent> onImageMessageEvent,
      @Nullable WebhookMessageEvent<LocationMessageContent> onLocationMessageEvent,
      @Nullable WebhookMessageEvent<AudioMessageContent> onAudioMessageEvent,
      @Nullable WebhookMessageEvent<VideoMessageContent> onVideoMessageEvent,
      @Nullable WebhookMessageEvent<StickerMessageContent> onStickerMessageEvent,
      @Nullable WebhookMessageEvent<FileMessageContent> onFileMessageEvent) {
    if (message instanceof TextMessageContent) {
      if (onTextMessageEvent != null) {
        onTextMessageEvent.callback((TextMessageContent) message);
      }
    } else if (message instanceof ImageMessageContent) {
      if (onImageMessageEvent != null) {
        onImageMessageEvent.callback((ImageMessageContent) message);
      }
    } else if (message instanceof LocationMessageContent) {
      if (onLocationMessageEvent != null) {
        onLocationMessageEvent.callback((LocationMessageContent) message);
      }
    } else if (message instanceof AudioMessageContent) {
      if (onAudioMessageEvent != null) {
        onAudioMessageEvent.callback((AudioMessageContent) message);
      }
    } else if (message instanceof VideoMessageContent) {
      if (onVideoMessageEvent != null) {
        onVideoMessageEvent.callback((VideoMessageContent) message);
      }
    } else if (message instanceof StickerMessageContent) {
      if (onStickerMessageEvent != null) {
        onStickerMessageEvent.callback((StickerMessageContent) message);
      }
    } else if (message instanceof FileMessageContent) {
      if (onFileMessageEvent != null) {
        onFileMessageEvent.callback((FileMessageContent) message);
      }
    }
  }

  public void parseLinePostbackEvent(
      @Nullable Map<String, String> params,
      @Nullable WebhookPostbackEvent<Void> onLinePostBackNone,
      @Nullable WebhookPostbackEvent<Date> onLinePostBackDate) {
    if (params == null || params.isEmpty()) {
      if (onLinePostBackNone != null) {
        onLinePostBackNone.callback(null);
      }
    } else if (params.containsKey("date")) {
      if (onLinePostBackDate != null) {
        onLinePostBackDate.callback(mTranslator.parseDate(params.get("date")));
      }
    } else if (params.containsKey("time")) {
      if (onLinePostBackDate != null) {
        onLinePostBackDate.callback(mTranslator.parseTime(params.get("time")));
      }
    } else if (params.containsKey("datetime")) {
      if (onLinePostBackDate != null) {
        onLinePostBackDate.callback(mTranslator.parseDatetime(params.get("datetime")));
      }
    }
  }
}
