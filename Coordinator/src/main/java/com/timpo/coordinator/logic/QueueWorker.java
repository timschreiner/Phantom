package com.timpo.coordinator.logic;

import com.google.common.base.Optional;
import com.timpo.common.Utils;
import com.timpo.messaging.interfaces.MessageSender;
import com.timpo.common.models.Task;
import com.timpo.coordinator.interfaces.TaskQueue;
import com.timpo.messaging.messaginginterfaces.CoordinatorToBroker;
import org.slf4j.Logger;

public class QueueWorker implements Runnable {

  private static final Logger LOG = Utils.logFor("QueueWorker");
  //
  private final TaskQueue queue;
  private final MessageSender<CoordinatorToBroker> coordinatorToBrokerSender;

  public QueueWorker(TaskQueue queue, MessageSender<CoordinatorToBroker> coordinatorToBrokerSender) {
    this.queue = queue;
    this.coordinatorToBrokerSender = coordinatorToBrokerSender;
  }

  public void run() {
    while (true) {
      //this will block until a task is available
      Task task = queue.retrieveNextTask();

      //TODO: what should we do for processing? test for Resources before attempting a job?
      //optimistically try to process the job
      coordinatorToBrokerSender.send().process(task);

      //see if there are any delayed tasks that need to go back into the
      //processing queue
      Optional<Task> delayedTask = queue.retrieveDelayedTask();
      if (delayedTask.isPresent()) {
        if (!queue.process(delayedTask.get())) {
          LOG.warn("unable to add delayed taskID=" + delayedTask.get().getID() + " back into the processing queue");
        }
      }

      //TODO: what kind of exceptions should we be catching? will an executor take care of dieing threads?
    }
  }
}
