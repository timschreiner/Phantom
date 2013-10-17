package com.timpo.messaging.implementations;

import com.timpo.messaging.implementations.DispatcherBroker;
import com.timpo.messaging.implementations.redis.RedisMessageAcknowledger;
import com.timpo.messaging.implementations.redis.RedisMessageReceiver;
import com.timpo.messaging.messaginginterfaces.ToBrokerMessages;
import com.timpo.messaging.interfaces.MessageAcknowledger;
import com.timpo.messaging.interfaces.MessageDispatcher;
import com.timpo.messaging.interfaces.MessageReceiver;
import redis.clients.jedis.JedisPool;

public class RedisReceiverBroker implements MessageReceiver<ToBrokerMessages> {

  private final RedisMessageReceiver<ToBrokerMessages> rmr;

  public RedisReceiverBroker(JedisPool pool, String receiverID,
          ToBrokerMessages handler) {

    MessageAcknowledger<ToBrokerMessages> ack =
            new RedisMessageAcknowledger<ToBrokerMessages>(pool, receiverID);

    MessageDispatcher<ToBrokerMessages> dispatcher = new DispatcherBroker(handler,
            ack);

    rmr = new RedisMessageReceiver<ToBrokerMessages>(pool, receiverID, dispatcher);
  }

  public void start() {
    rmr.start();
  }

  public void stop() {
    rmr.stop();
  }
}
