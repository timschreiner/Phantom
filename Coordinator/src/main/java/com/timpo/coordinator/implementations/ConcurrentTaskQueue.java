package com.timpo.coordinator.implementations;

import com.google.common.base.Optional;
import com.timpo.common.models.Task;
import com.timpo.coordinator.interfaces.TaskQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConcurrentTaskQueue implements TaskQueue {

  private final PriorityBlockingQueue<Task> workQueue;
  private final DelayQueue<Task> delayQueue;

  public ConcurrentTaskQueue(PriorityBlockingQueue<Task> workQueue, DelayQueue<Task> delayQueue) {
    this.workQueue = workQueue;
    this.delayQueue = delayQueue;
  }

  @Override
  public boolean process(Task task) {
    return workQueue.offer(task);
  }

  @Override
  public Task retrieveNextTask() {
    //NOTE: this will block indefinitely until a Task is available
    return workQueue.poll();
  }

  @Override
  public boolean delayProcessing(Task taskToDelay, long delay, TimeUnit timeUnit) {
    taskToDelay.delayFor(delay, timeUnit);
    return delayQueue.offer(taskToDelay);
  }

  @Override
  public boolean endProcessingDelay(Task delayedTask) {
    return delayQueue.remove(delayedTask);
  }

  public Optional<Task> retrieveDelayedTask() {
    try {
      Task task = delayQueue.poll(1L, TimeUnit.SECONDS);
      if (task == null) {
        return Optional.absent();
      } else {
        return Optional.of(task);
      }

    } catch (InterruptedException ex) {
      return Optional.absent();
    }
  }
}
