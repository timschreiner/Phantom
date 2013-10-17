package com.timpo.messaging.interfaces;

/**
 *
 * @param <T> the message interface this sender sends messages to
 */
public interface MessageSender<T extends MessagingInterface> {

  /**
   * Expose the message interface so we can send messages to it
   *
   * @return
   */
  public T send();

  public abstract void start();

  public abstract void stop();
}
