package com.timpo.messaging.interfaces;

/**
 * Allows a dispatcher to acknowledge that a message has been received and
 * processed
 */
public abstract class MessageAcknowledger<T extends MessagingInterface> {

  /**
   * Durable messages should call this after processing a message, otherwise it
   * might get processed again.
   *
   * @param messageID the id of the message
   *
   * @return whether or not the message had already been acknowledged
   */
  public abstract boolean acknowledgeMessage(String messageID);
}
