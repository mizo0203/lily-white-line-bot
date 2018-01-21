package com.mizo0203.lilywhite.domain;

import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.DatetimePickerAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.Template;
import com.mizo0203.lilywhite.repo.Repository;
import com.mizo0203.lilywhite.repo.State;
import com.mizo0203.lilywhite.repo.line.messaging.data.webHook.event.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
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
    mRepository.pushMessage(source_id, new TextMessage(message), createTemplateMessageToReset());
  }

  private Message createTemplateMessageToReset() {
    return new TemplateMessage(
        "テンプレートメッセージはiOS版およびAndroid版のLINE 6.7.0以降で対応しています。", createTemplateToReset());
  }

  private Template createTemplateToReset() {
    return new ButtonsTemplate(
        null, null, "リマインダーをリセットしますよー", createPostBackActionsToRequestReset());
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

  private com.linecorp.bot.model.message.template.Template
      createButtonsTemplateToRequestReminderDate() {
    return com.linecorp.bot.model.message.template.ButtonsTemplate.builder()
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
        replyToken, new TextMessage("リマインダーをセットしましたよー\n" + mTranslator.formatDate(date)));
  }

  public RequestBody getRequestBody(HttpServletRequest req) {
    return mRepository.getRequestBody(req);
  }
}
