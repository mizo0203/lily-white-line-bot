package com.mizo0203.lilywhite.domain;

import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.mizo0203.lilywhite.repo.Repository;
import com.mizo0203.lilywhite.repo.State;
import com.mizo0203.lilywhite.repo.line.messaging.data.MessageObject;
import com.mizo0203.lilywhite.repo.line.messaging.data.TemplateMessageObject;
import com.mizo0203.lilywhite.repo.line.messaging.data.TextMessageObject;
import com.mizo0203.lilywhite.repo.line.messaging.data.action.Action;
import com.mizo0203.lilywhite.repo.line.messaging.data.action.DateTimePickerAction;
import com.mizo0203.lilywhite.repo.line.messaging.data.action.PostBackAction;
import com.mizo0203.lilywhite.repo.line.messaging.data.template.ButtonTemplate;
import com.mizo0203.lilywhite.repo.line.messaging.data.template.Template;
import com.mizo0203.lilywhite.repo.line.messaging.data.webHook.event.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UseCase {
  public static final String ACTION_DATA_REQUEST_REMINDER_DATE_SET =
      "ACTION_DATA_REQUEST_REMINDER_DATE_SET";
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
        source_id,
        new MessageObject[] {
          new TextMessageObject(message), createTemplateMessageObjectToReset(),
        });
  }

  private MessageObject createTemplateMessageObjectToReset() {
    Template template =
        new ButtonTemplate(
            "リマインダーをリセットしますよー",
            new Action[] {
              createPostBackActionToRequestReset(),
            });
    return new TemplateMessageObject("テンプレートメッセージはiOS版およびAndroid版のLINE 6.7.0以降で対応しています。", template);
  }

  private Action createPostBackActionToRequestReset() {
    return new PostBackAction(ACTION_DATA_REQUEST_RESET).label("リセット");
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

  private com.linecorp.bot.model.message.template.Template
      createButtonsTemplateToRequestReminderDate() {
    return com.linecorp.bot.model.message.template.ButtonsTemplate.builder()
        .text("リマインダー日時をセットしますよー")
        .actions(createDateTimePickerActionsToRequestReminderDate())
        .build();
  }

  private List<com.linecorp.bot.model.action.Action>
      createDateTimePickerActionsToRequestReminderDate() {
    return Arrays.asList(
        new com.linecorp.bot.model.action.DatetimePickerAction[] {
          new com.linecorp.bot.model.action.DatetimePickerAction(
              "セット",
              ACTION_DATA_REQUEST_REMINDER_DATE_SET,
              DateTimePickerAction.Mode.DATE_TIME.toString()),
        });
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
        replyToken, new TextMessage("リマインダーをセットしましたよー\n" + mTranslator.formatDate(date)));
  }

  public RequestBody getRequestBody(HttpServletRequest req) {
    return mRepository.getRequestBody(req);
  }
}
