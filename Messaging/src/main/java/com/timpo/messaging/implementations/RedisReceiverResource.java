package com.timpo.messaging.implementations;

import com.timpo.messaging.implementations.redis.RedisMessageAcknowledger;
import com.timpo.messaging.implementations.redis.RedisMessageReceiver;
import com.timpo.messaging.messaginginterfaces.ToResourceMessages;
import com.timpo.messaging.interfaces.MessageAcknowledger;
import com.timpo.messaging.interfaces.MessageDispatcher;
import com.timpo.messaging.interfaces.MessageReceiver;
import redis.clients.jedis.JedisPool;

public class RedisReceiverResource implements MessageReceiver<ToResourceMessages> {

  private final RedisMessageReceiver<ToResourceMessages> rmr;

  public RedisReceiverResource(JedisPool pool, String receiverID,
          ToResourceMessages handler) {

    MessageAcknowledger<ToResourceMessages> ack =
            new RedisMessageAcknowledger<ToResourceMessages>(pool, receiverID);

    MessageDispatcher<ToResourceMessages> dispatcher =
            new DispatcherResource(handler, ack);

    rmr = new RedisMessageReceiver<ToResourceMessages>(pool, receiverID, dispatcher);
  }

  public void start() {
    rmr.start();
  }

  public void stop() {
    rmr.stop();
  }
}
