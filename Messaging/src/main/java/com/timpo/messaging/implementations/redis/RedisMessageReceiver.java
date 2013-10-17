package com.timpo.messaging.implementations.redis;

import com.timpo.messaging.interfaces.MessageDispatcher;
import com.timpo.messaging.interfaces.MessageReceiver;
import com.timpo.messaging.interfaces.MessagingInterface;
import redis.clients.jedis.JedisPool;

public class RedisMessageReceiver<T extends MessagingInterface> implements MessageReceiver<T> {

  private final Thread receiverThread;
  private final RedisIncomingMessageHandler<T> dispatchRunnable;

  public RedisMessageReceiver(JedisPool pool, String receiverID,
          MessageDispatcher<T> dispatcher) {

    dispatchRunnable = new RedisIncomingMessageHandler<T>(pool, dispatcher, receiverID);

    receiverThread = new Thread(dispatchRunnable);
  }

  @Override
  public void start() {
    receiverThread.start();
  }

  @Override
  public void stop() {
    dispatchRunnable.stop();
  }
}
