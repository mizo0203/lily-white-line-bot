package com.mizo0203.lilywhite;

import com.mizo0203.lilywhite.domain.UseCase;
import com.mizo0203.lilywhite.repo.State;
import com.mizo0203.lilywhite.repo.line.messaging.data.webHook.event.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.logging.Logger;

public class LilyWhiteLineBotServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LilyWhiteLineBotServlet.class.getName());

  private UseCase mUseCase;

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    onLineWebhook(req, resp);
  }

  /**
   * LINE Platform からのリクエストを受信
   *
   * <p>友だち追加やメッセージの送信のようなイベントがトリガーされると、webhook URL に HTTPS POST リクエストが送信されます。Webhook URL
   * はチャネルに対してコンソールで設定します。
   *
   * <p>リクエストはボットアプリのサーバーで受信および処理されます。
   *
   * @param req an {@link HttpServletRequest} object that contains the request the client has made
   *     of the servlet
   * @param resp an {@link HttpServletResponse} object that contains the response the servlet sends
   *     to the client
   */
  private void onLineWebhook(HttpServletRequest req, HttpServletResponse resp) {
    mUseCase = new UseCase();
    try {
      RequestBody requestBody = mUseCase.getRequestBody(req);
      if (requestBody == null) {
        return;
      }
      for (WebHookEventObject event : requestBody.concreteWebHookEventObject()) {
        if (event instanceof JoinEvent) {
          onLineJoin((JoinEvent) event);
        } else if (event instanceof MessageEvent) {
          onLineMessage((MessageEvent) event);
        } else if (event instanceof PostBackEvent) {
          onLinePostBack((PostBackEvent) event);
        }
      }
    } finally {
      // ボットアプリのサーバーに webhook から送信される HTTP POST リクエストには、ステータスコード 200 を返す必要があります。
      // https://developers.line.me/ja/docs/messaging-api/reference/#anchor-99cdae5b4b38ad4b86a137b508fd7b1b861e2366
      resp.setStatus(HttpServletResponse.SC_OK);
      mUseCase.destroy();
    }
  }

  private void onLineJoin(JoinEvent event) {
    LOG.info("replyToken: " + event.getReplyToken());
    mUseCase.initSource(event.getSource().getSourceId());
    mUseCase.replyMessageToRequestReminderMessage(event.getReplyToken());
  }

  private void onLineMessage(MessageEvent event) {
    LOG.info("text: " + event.getMessage().text);
    if (event.getMessage().text == null) {
      return;
    }
    State state = mUseCase.getState(event.getSource().getSourceId());
    switch (state) {
      case NO_REMINDER_MESSAGE:
        {
          String reminderMessage = event.getMessage().text.split("\n")[0];
          mUseCase.setReminderMessage(event.getSource().getSourceId(), reminderMessage);
          mUseCase.replyMessageToRequestReminderDate(event.getReplyToken());
          break;
        }
      case HAS_REMINDER_MESSAGE:
      case REMINDER_ENQUEUED:
      case REMINDER_CANCELLATION_CONFIRM:
        {
          // NOP
          break;
        }
      default:
        break;
    }
  }

  private void onLinePostBack(PostBackEvent event) {
    State state = mUseCase.getState(event.getSource().getSourceId());
    switch (state) {
      case NO_REMINDER_MESSAGE:
        {
          if (UseCase.ACTION_DATA_REQUEST_RESET.equals(event.getPostBackData())) {
            mUseCase.replyMessageToRequestReminderMessage(event.getReplyToken());
          }
          break;
        }
      case HAS_REMINDER_MESSAGE:
        {
          if (UseCase.ACTION_DATA_REQUEST_REMINDER_DATE_SET.equals(event.getPostBackData())) {
            Date date = event.getPostBackParams().parseDatetime();
            mUseCase.enqueueReminderTask(event.getSource().getSourceId(), date);
            mUseCase.replyReminderConfirmMessage(event.getReplyToken(), date);
          }
          break;
        }
      case REMINDER_ENQUEUED:
        {
          if (UseCase.ACTION_DATA_REQUEST_REMINDER_CANCELLATION.equals(event.getPostBackData())) {
            mUseCase.setCancellationConfirm(event.getSource().getSourceId(), true);
            mUseCase.replyReminderCancellationConfirmMessage(event.getReplyToken());
          }
          break;
        }
      case REMINDER_CANCELLATION_CONFIRM:
        {
          if (UseCase.ACTION_DATA_CANCEL_REMINDER.equals(event.getPostBackData())) {
            mUseCase.replyCanceledReminderMessage(event.getReplyToken());
            mUseCase.initSource(event.getSource().getSourceId());
          } else if (UseCase.ACTION_DATA_NOT_CANCEL_REMINDER.equals(event.getPostBackData())) {
            mUseCase.setCancellationConfirm(event.getSource().getSourceId(), false);
            mUseCase.replyNotCanceledReminderMessage(event.getReplyToken());
          }
          break;
        }
      default:
        break;
    }
  }
}
