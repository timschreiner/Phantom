package com.timpo.broker.interfaces;

import com.timpo.common.models.Task;

public interface Foreman {

  /**
   * Attempt to lock a task to a resource. Only succeeds and returns true if the
   * task is unclaimed; otherwise, it fails and returns false
   *
   * @param taskID the id for the task to process
   *
   * @param resourceID the resource claiming the task
   *
   * @return whether or not this resource claimed the task first
   */
  public boolean claimTask(String taskID, String resourceID);

  /**
   * Determines whether a particular class has been claimed yet or not
   *
   * @param taskID the id for the task that might be claimed by a resource
   *
   * @return whether the task has been claimed
   */
  public boolean taskClaimed(String taskID);

  /**
   * Alerts a particular Resource that it should process a job
   *
   * @param task the task to process
   *
   * @param resourceID the Resource that should do the processing
   */
  public void offerTask(Task task, String resourceID);

  /**
   * Alerts a particular Resource that it should process a job
   *
   * @param taskID the id for the task to process
   *
   * @param resourceID the Resource that should do the processing
   */
  public void assignTask(String taskID, String resourceID);

  /**
   * Create a place where the highest claims can be tracked
   *
   * @param taskID the claim tracker to create
   */
  public void createClaimsTracker(String taskID);

  /**
   * Reclaim the memory being used by the claims tracker
   *
   * @param taskID the claim tracker to delete
   */
  public void cleanupClaimsTracker(String taskID);
}
