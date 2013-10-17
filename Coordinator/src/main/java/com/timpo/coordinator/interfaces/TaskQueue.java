package com.timpo.coordinator.interfaces;

import com.google.common.base.Optional;
import com.timpo.common.models.Task;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for processing tasks
 */
public interface TaskQueue {

  /**
   * Begin the processing cycle for this task
   *
   * @param task to process
   *
   * @return whether the queue has the capacity to store this task
   */
  public boolean process(Task task);

  /**
   * @return the highest priority task that isn't being delayed
   */
  public Task retrieveNextTask();

  /**
   * Delay the processing of this task for delay TimeUnits
   *
   * @param taskToDelay
   *
   * @param delay
   *
   * @param timeUnit
   *
   * @return whether the queue has the capacity to store this task
   */
  public boolean delayProcessing(Task taskToDelay, long delay, TimeUnit timeUnit);

  //TODO: make sure that if you update the delay of something in a queue, the queue automatically reorders it...
  /**
   * End the delay associated with this task
   *
   * @param delayedTask
   */
  public boolean endProcessingDelay(Task delayedTask);

  /**
   * Get the task whose delay has been finished the longest
   *
   * @return delayed task
   */
  public Optional<Task> retrieveDelayedTask();
}
