package com.mizo0203.lilywhite.repo;

import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.Message;
import com.mizo0203.lilywhite.repo.line.messaging.data.MessageObject;
import com.mizo0203.lilywhite.repo.line.messaging.data.webHook.event.RequestBody;
import com.mizo0203.lilywhite.repo.objectify.entity.KeyEntity;
import com.mizo0203.lilywhite.repo.objectify.entity.LineTalkRoomConfig;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.logging.Logger;

public class Repository {

  private static final Logger LOG = Logger.getLogger(Repository.class.getName());
  private final OfyRepository mOfyRepository;
  private final LineRepository mLineRepository;
  private final PushQueueRepository mPushQueueRepository;

  public Repository() {
    mOfyRepository = new OfyRepository();
    // FIXME: getChannelAccessToken() をクラスメソッドに変更
    mLineRepository = new LineRepository(getChannelAccessToken());
    mPushQueueRepository = new PushQueueRepository();
  }

  public void destroy() {
    mOfyRepository.destroy();
    mLineRepository.destroy();
    mPushQueueRepository.destroy();
  }

  public State getState(String sourceId) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(sourceId);
    if (config.isReminderEnqueued()) {
      return State.REMINDER_ENQUEUED;
    } else if (config.getReminderMessage() != null) {
      return State.HAS_REMINDER_MESSAGE;
    } else {
      return State.NO_REMINDER_MESSAGE;
    }
  }

  public void setReminderMessage(String sourceId, String reminderMessage) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(sourceId);
    config.setReminderMessage(reminderMessage);
    mOfyRepository.saveLineTalkRoomConfig(config);
  }

  public void clearEvent(String sourceId) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(sourceId);
    deleteReminderTask(config);
    mOfyRepository.deleteLineTalkRoomConfig(sourceId);
  }

  private void deleteReminderTask(LineTalkRoomConfig config) {
    String taskName = config.getReminderEnqueuedTaskName();
    if (taskName == null || taskName.isEmpty()) {
      return;
    }
    mPushQueueRepository.deleteReminderTask(taskName);
    config.setReminderEnqueuedTaskName(null);
  }

  private LineTalkRoomConfig getOrCreateLineTalkRoomConfig(String sourceId) {
    LineTalkRoomConfig config = mOfyRepository.loadLineTalkRoomConfig(sourceId);
    if (config == null) {
      config = new LineTalkRoomConfig(sourceId);
    }
    return config;
  }

  private String getChannelAccessToken() {
    KeyEntity keyEntity = mOfyRepository.loadKeyEntity("ChannelAccessToken");

    if (keyEntity == null) {
      keyEntity = new KeyEntity();
      keyEntity.key = "ChannelAccessToken";
      keyEntity.value = "";
      mOfyRepository.saveKeyEntity(keyEntity);
    }

    if (keyEntity.value.isEmpty()) {
      LOG.severe("ChannelAccessToken isEmpty");
    }

    return keyEntity.value;
  }

  private String getChannelSecret() {
    KeyEntity keyEntity = mOfyRepository.loadKeyEntity("ChannelSecret");

    if (keyEntity == null) {
      keyEntity = new KeyEntity();
      keyEntity.key = "ChannelSecret";
      keyEntity.value = "";
      mOfyRepository.saveKeyEntity(keyEntity);
    }

    if (keyEntity.value.isEmpty()) {
      LOG.severe("ChannelSecret isEmpty");
    }

    return keyEntity.value;
  }

  /**
   * 応答メッセージを送る
   *
   * @param replyToken Webhook で受信する応答トークン
   * @param messages 送信するメッセージ (最大件数：5)
   */
  public void replyMessage(String replyToken, Message... messages) {
    mLineRepository.replyMessage(new ReplyMessage(replyToken, Arrays.asList(messages)));
  }

  /**
   * プッシュメッセージを送る
   *
   * @param to 送信先のID。Webhookイベントオブジェクトで返される、userId、groupId、またはroomIdの値を使用します。LINEアプリに表示されるLINE
   *     IDは使用しないでください。
   * @param messages 送信するメッセージ 最大件数：5
   */
  public void pushMessage(String to, MessageObject[] messages) {
    String channelAccessToken = getChannelAccessToken();
    mLineRepository.pushMessage(channelAccessToken, to, messages);
  }

  /**
   * グループメンバーのユーザーIDを取得する
   *
   * @param groupId グループID。Webhookイベントオブジェクトのsourceオブジェクトで返されます。
   */
  @SuppressWarnings("unused")
  public void idsMembersGroup(String groupId) {
    String channelAccessToken = getChannelAccessToken();
    mLineRepository.idsMembersGroup(channelAccessToken, groupId);
  }

  /**
   * リクエストボディを取得する
   *
   * @param req an {@link HttpServletRequest} object that contains the request the client has made
   *     of the servlet
   * @return リクエストボディは、webhookイベントオブジェクトの配列を含むJSONオブジェクトです。
   */
  public RequestBody getRequestBody(HttpServletRequest req) {
    return mLineRepository.getRequestBody(getChannelSecret(), req);
  }

  public void enqueueReminderTask(String sourceId, long etaMillis) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(sourceId);
    String taskName =
        mPushQueueRepository.enqueueReminderTask(
            config.getSourceId(), etaMillis, config.getReminderMessage());
    LOG.info("enqueueReminderTask taskName: " + taskName);
    config.setReminderEnqueuedTaskName(taskName);
    mOfyRepository.saveLineTalkRoomConfig(config);
  }
}
