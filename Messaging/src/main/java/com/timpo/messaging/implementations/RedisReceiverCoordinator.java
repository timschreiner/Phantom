package com.timpo.messaging.implementations;

import com.timpo.messaging.implementations.redis.RedisMessageAcknowledger;
import com.timpo.messaging.implementations.redis.RedisMessageReceiver;
import com.timpo.messaging.messaginginterfaces.ToCoordinatorMessages;
import com.timpo.messaging.interfaces.MessageAcknowledger;
import com.timpo.messaging.interfaces.MessageDispatcher;
import com.timpo.messaging.interfaces.MessageReceiver;
import redis.clients.jedis.JedisPool;

public class RedisReceiverCoordinator implements MessageReceiver<ToCoordinatorMessages> {

  private final RedisMessageReceiver<ToCoordinatorMessages> rmr;

  public RedisReceiverCoordinator(JedisPool pool, String receiverID,
          ToCoordinatorMessages handler) {

    MessageAcknowledger<ToCoordinatorMessages> ack =
            new RedisMessageAcknowledger<ToCoordinatorMessages>(pool, receiverID);

    MessageDispatcher<ToCoordinatorMessages> dispatcher =
            new DispatcherCoordinator(handler, ack);

    rmr = new RedisMessageReceiver<ToCoordinatorMessages>(pool, receiverID, dispatcher);
  }

  public void start() {
    rmr.start();
  }

  public void stop() {
    rmr.stop();
  }
}
