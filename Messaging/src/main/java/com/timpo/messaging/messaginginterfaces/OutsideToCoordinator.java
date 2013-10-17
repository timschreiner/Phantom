package com.timpo.messaging.messaginginterfaces;

import com.timpo.messaging.interfaces.MessagingInterface;

/**
 * For incoming {@link Job}s that need to be processed
 */
public interface OutsideToCoordinator extends MessagingInterface {

  /**
   * @param stringJob string representation (json, xml, etc.) of a {@link Job}
   * that needs processing
   */
  public void incomingJobRequest(String stringJob);

  /**
   * Enable/disable processing of incoming jobs. Useful when you want to bring a
   * JobCoordinator down gracefully by letting it finish its existing work first
   *
   * @param accept
   */
  public void acceptIncomingJobs(boolean accept);
}
