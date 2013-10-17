package com.timpo.messaging.messaginginterfaces;

import com.timpo.messaging.interfaces.MessagingInterface;
import com.timpo.common.models.Task;

/**
 * For messages from a Coordinator to a Broker
 */
public interface CoordinatorToBroker extends MessagingInterface {

  /**
   * Requests whether the broker currently has Resources capable of processing
   * this task, based on the tasks requirements.
   * <br/>
   * <br/>
   * The broker asynchronously responds using canProcess (success) or
   * cannotProcess (failure)
   *
   * @param task a Task to be processed
   */
  public void canProcess(Task task);

  /**
   * Requests that the broker attempt to process this task by assigning it to an
   * available resource.
   * <br/>
   * <br/>
   * The broker asynchronously calls processing (success) or
   * cannotProcess(failure) on the registered {@link BrokerResponseHandler}
   *
   * @param task a Task to be processed
   */
  public void process(Task task);
}
