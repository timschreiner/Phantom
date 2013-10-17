package com.timpo.messaging.messaginginterfaces;

import com.timpo.messaging.interfaces.MessagingInterface;
import com.timpo.common.models.Task;
import java.util.Map;
import java.util.Set;

/**
 * For messages from a Broker to a Resource
 */
public interface BrokerToResource extends MessagingInterface {

  /**
   * Requests that multiple resources calculate a bid that will be used to
   * determine which resource is best able to perform a job.
   *
   * @param requirements the requirements that the Resource will calculate a bid
   * for
   *
   * @param taskID the task we want to get bids for
   *
   * @param resourceIDs multiple resources to get a bid from
   */
  public void requestBids(Map<String, String> requirements, String taskID, Set<String> resourceIDs);

  /**
   * Offers a particular Resource the opportunity to process a task. Any
   * resource that agrees to process this task should not respond to other
   * offers for other tasks until it receives either processTask or cancelOffer.
   *
   * @param task the task being offered
   *
   * @param resourceID the Resource the offer is being made to
   */
  public void offer(Task task, String resourceID);

  /**
   * Alerts a particular Resource that it should process a task
   *
   * @param taskID the id of the task to process. The resource should already
   * have the Task object associated with this taskID
   *
   * @param resourceID the Resource that should do the processing
   */
  public void processTask(String taskID, String resourceID);

  /**
   * Alerts a particular Resource that another resource is processing this task
   * and it should look for other work.
   *
   * @param taskID the id of the task to forget about processing.
   *
   * @param resourceID the Resource that should forget about processing the
   * task.
   */
  public void cancelOffer(String taskID, String resourceID);
}
