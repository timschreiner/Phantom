package com.timpo.messaging.interfaces;

/**
 * Takes a string message and calls a corresponding method on a handler
 *
 * @param <T> the type of the handler
 */
public abstract class MessageDispatcher<T extends MessagingInterface> {

  private final T handler;
  private final MessageAcknowledger<T> messageAcknowledger;

  public abstract void dispatch(String message);

  //<editor-fold defaultstate="collapsed" desc="generated">
  public MessageDispatcher(T handler, MessageAcknowledger<T> messageAcknowledger) {
    this.handler = handler;
    this.messageAcknowledger = messageAcknowledger;
  }

  protected T getHandler() {
    return handler;
  }

  public MessageAcknowledger getMessageAcknowledger() {
    return messageAcknowledger;
  }
  //</editor-fold>
}