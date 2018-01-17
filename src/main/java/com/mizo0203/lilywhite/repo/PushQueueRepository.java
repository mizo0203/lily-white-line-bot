package com.mizo0203.lilywhite.repo;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.mizo0203.lilywhite.push_task.ReminderTaskServlet;

import java.util.logging.Logger;

/* package */ class PushQueueRepository {

  private static final Logger LOG = Logger.getLogger(PushQueueRepository.class.getName());
  private final Queue mQueue;

  PushQueueRepository() {
    mQueue = QueueFactory.getDefaultQueue();
  }

  @SuppressWarnings("EmptyMethod")
  public void destroy() {
    // NOP
  }

  protected void enqueueReminderTask(String source_id, long etaMillis, String message) {
    LOG.info("enqueueReminderTask");
    mQueue.add(
        TaskOptions.Builder.withUrl("/push_task/reminder_task")
            .param(ReminderTaskServlet.PARAM_NAME_SOURCE_ID, source_id)
            .param(ReminderTaskServlet.PARAM_NAME_MESSAGE, message)
            .etaMillis(etaMillis));
  }
}
