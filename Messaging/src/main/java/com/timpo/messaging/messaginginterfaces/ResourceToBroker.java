package com.timpo.messaging.messaginginterfaces;

import com.timpo.messaging.interfaces.MessagingInterface;
import java.util.Map;

/**
 * For messages from a Resource to a Broker
 */
public interface ResourceToBroker extends MessagingInterface {

  /**
   * Make a bid for a particular task
   *
   *
   * @param taskID the Task being bid on
   *
   * @param ResourceID the resource making the bid
   *
   * @param bid a score that represents how well the resource believes they can
   * process the task
   */
  public void placeBid(String taskID, String resourceID, double bid);

  /**
   * Response indicating that the resource would like to process this task
   *
   * @param taskID the id of the task to be claimed for processing
   *
   * @param resourceID the id of the Resource claiming the task
   */
  public void claimTask(String taskID, String resourceID);

  /**
   * Response indicating that the process request has been successfully assigned
   *
   * @param taskID the id of the task being processed
   *
   * @param resourceID the id of the Resource being used to process this task
   */
  public void processingTask(String taskID, String resourceID);

  /**
   * Response indicating a request has finished and that the corresponding task
   * needs to update its state and params.
   *
   * @param taskID an ID that {@link Storage} can use to find the task this
   * message corresponds to
   *
   * @param newState the state the task should be changed to
   *
   * @param newParams any new params that should be merged into the task
   */
  public void finishedProcessing(String taskID, String resourceID, String newState, Map<String, Object> newParams);
}
