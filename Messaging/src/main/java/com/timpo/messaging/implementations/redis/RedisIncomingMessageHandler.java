package com.timpo.messaging.implementations.redis;

import com.timpo.messaging.interfaces.MessageDispatcher;
import com.timpo.common.Constants;
import com.timpo.messaging.interfaces.MessagingInterface;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisIncomingMessageHandler<T extends MessagingInterface> implements Runnable {

  private final JedisPool pool;
  private final String receiverID;
  private final MessageDispatcher<T> dispatcher;
  private final ExecutorService executorService;
  private volatile boolean keepRunning;

  public RedisIncomingMessageHandler(JedisPool pool, MessageDispatcher<T> dispatcher, String receiverID) {
    this.pool = pool;
    this.dispatcher = dispatcher;
    this.receiverID = receiverID;

    keepRunning = true;
    executorService = Executors.newCachedThreadPool();
  }

  public void run() {
    String incomingMessagesKey = RedisMessageHelpers.makeIncomingMessageKey(receiverID);
    int timeOut = Constants.Defaults.REDIS_BLOCK_TIMEOUT;

    Jedis client = pool.getResource();
    try {
      while (keepRunning) {
        List<String> messages = client.blpop(timeOut, incomingMessagesKey);

        for (final String message : messages) {
          //for any messages that come back, throw them into the executor thread
          //pool inside a dispatcher that knows how to handle them
          executorService.execute(new Runnable() {
            public void run() {
              dispatcher.dispatch(message);
            }
          });
        }
      }

    } finally {
      pool.returnResource(client);
      pool.destroy();
    }
  }

  public final void stop() {
    keepRunning = false;
  }
}
