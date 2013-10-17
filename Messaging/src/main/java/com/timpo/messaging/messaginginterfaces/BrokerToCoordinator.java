package com.timpo.messaging.messaginginterfaces;

import com.timpo.messaging.interfaces.MessagingInterface;
import java.util.Map;

/**
 * For messages from a Broker to a Coordinator
 */
public interface BrokerToCoordinator extends MessagingInterface {

  /**
   * Response from a broker indicating that the Resources required to process
   * this request are available
   *
   * @param taskID an ID that {@link Storage} can use to find the task this
   * message corresponds to
   */
  public void resourcesAvailable(String taskID);

  /**
   * Response from a broker indicating that no Resources are available to
   * process the request at this time
   *
   * @param taskID an ID that {@link Storage} can use to find the task this
   * message corresponds to
   */
  public void resourcesUnavailable(String taskID);

  /**
   * Response indicating that the process request has been successfully assigned
   *
   * @param taskID an ID that {@link Storage} can use to find the task this
   * message corresponds to
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
  public void taskFinished(String taskID, String newState, Map<String, Object> newParams);
}
