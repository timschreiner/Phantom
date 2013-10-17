package com.timpo.messaging.implementations.redis;

import com.timpo.messaging.interfaces.MessageAcknowledger;
import com.timpo.messaging.interfaces.MessagingInterface;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisMessageAcknowledger<T extends MessagingInterface> extends MessageAcknowledger {

  private final String acknowledgmentKey;
  private final JedisPool pool;

  public RedisMessageAcknowledger(JedisPool pool, String receiverID) {
    this.pool = pool;

    acknowledgmentKey = RedisMessageHelpers.makeMessageAcknowledgmentKey(receiverID);
  }

  @Override
  public boolean acknowledgeMessage(String messageID) {
    Jedis client = pool.getResource();
    try {
      //redis returns a 1 if the delete is succesful
      return client.hdel(acknowledgmentKey, messageID) == 1;

    } finally {
      pool.returnResource(client);
    }
  }
}
