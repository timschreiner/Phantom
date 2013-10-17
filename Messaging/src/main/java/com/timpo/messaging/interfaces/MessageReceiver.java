package com.timpo.messaging.interfaces;

/**
 * Receives messages for a particular MessagingInterface and dispatches them to
 * a handler.
 *
 */
public interface MessageReceiver<T extends MessagingInterface> {

  /**
   * Setup any resources required for messaging.
   */
  public void start();

  /**
   * Teardown any resources required for messaging.
   */
  public void stop();
}
