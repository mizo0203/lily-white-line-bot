package com.mizo0203.lilywhite.domain;

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
import java.util.Date;

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
        replyToken,
        new MessageObject[] {
          new TextMessageObject("リマインダーをセットしますよー\nメッセージを返信してくださーい\n例) 春ですよー"),
        });
  }

  public void replyMessageToRequestReminderDate(String replyToken) {
    mRepository.replyMessage(
        replyToken,
        new MessageObject[] {
          createTemplateMessageObjectToRequestReminderDate(),
        });
  }

  private MessageObject createTemplateMessageObjectToRequestReminderDate() {
    Template template =
        new ButtonTemplate(
            "リマインダー日時をセットしますよー",
            new Action[] {
              createDateTimePickerActionToRequestReminderDate(),
            });
    return new TemplateMessageObject("テンプレートメッセージはiOS版およびAndroid版のLINE 6.7.0以降で対応しています。", template);
  }

  private Action createDateTimePickerActionToRequestReminderDate() {
    return new DateTimePickerAction(
            ACTION_DATA_REQUEST_REMINDER_DATE_SET, DateTimePickerAction.Mode.DATE_TIME)
        .label("セット");
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
        new MessageObject[] {
          new TextMessageObject("リマインダーをセットしましたよー\n" + mTranslator.formatDate(date)),
        });
  }

  public RequestBody getRequestBody(HttpServletRequest req) {
    return mRepository.getRequestBody(req);
  }
}
