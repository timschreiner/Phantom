package com.timpo.messaging.implementations.redis;

import com.timpo.common.Utils;
import com.timpo.common.models.InvokeMethodMessage;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

public abstract class RedisMessageSender {

  private static final Logger LOG = Utils.logFor("RedisMessageSender");
  //
  public static final int EPHEMERAL_MESSAGE_EXPIRATION = 15;
  public static final TimeUnit EXPIRATION_INTERVAL = TimeUnit.SECONDS;
  //
  private final JedisPool pool;
  private final String from;

  public RedisMessageSender(JedisPool pool, String from) {
    this.pool = pool;
    this.from = from;
  }

  public final void start() {
    //nothing needs to be done, the pool is already created
  }

  public final void stop() {
    //free any connections used by the pool
    pool.destroy();
  }

  protected final String from() {
    return from;
  }

  /**
   * Send a message that can be retrieved multiple times until a receiver
   * acknowledges it
   *
   * @param receiverID the intended recipient
   * @param method the MessageInterface method the message represents
   * @param params the method params being encoded
   */
  protected final void sendDurableMessage(String receiverID, String method, Map<String, Object> params) {
    String messageID = Utils.generateUniqueID();

    String durableMessagesKey = RedisMessageHelpers.makeMessageAcknowledgmentKey(receiverID);
    String incomingMessagesKey = RedisMessageHelpers.makeIncomingMessageKey(receiverID);

    Jedis client = pool.getResource();
    try {
      String message = InvokeMethodMessage.durable(method, from, params, messageID).toJson();

      //use a transaction so that the message is definitely in both places it
      //needs to be
      Transaction multi = client.multi();

      //put the message in a hash so we can use the existance of it to determine
      //whether a message has been ACK'd or not
      multi.hset(durableMessagesKey, messageID, message);

      //add the message to a list so a receiver can use LBPOP to block until
      //new messages appear
      multi.lpush(incomingMessagesKey, message);

      //close the transaction
      multi.exec();

    } catch (Exception ex) {
      LOG.warn("problem with sendDurableMessage({},{},{}) - {}",
              receiverID, method, params, Utils.getLoggableException(ex));

    } finally {
      pool.returnResource(client);
    }
  }

  /**
   * Send a message that can only be retrieved once
   *
   * @param receiverID the intended recipient
   * @param method the MessageInterface method the message represents
   * @param params the method params being encoded
   */
  protected final void sendEphemeralMessage(String receiverID, String method, Map<String, Object> content) {
    Long expiration = Utils.fromNow(EPHEMERAL_MESSAGE_EXPIRATION, EXPIRATION_INTERVAL);

    String incomingMessagesKey = RedisMessageHelpers.makeIncomingMessageKey(receiverID);

    Jedis client = pool.getResource();
    try {
      String message = InvokeMethodMessage.ephemeral(method, from, content, expiration).toJson();
      //add the message to a list so a receiver can use LBPOP to block until
      //new messages appear
      client.lpush(incomingMessagesKey, message);

    } catch (Exception ex) {
      LOG.warn("problem with sendEphemeralMessage({},{},{}) - {}",
              receiverID, method, content, Utils.getLoggableException(ex));

    } finally {
      pool.returnResource(client);
    }
  }

  /**
   * Send a message to multiple recipients that can only be retrieved once for
   * each resource
   *
   * @param receiverIDs the intended recipients
   * @param method the MessageInterface method the message represents
   * @param params the method params being encoded
   */
  protected final void sendEphemeralMessages(Set<String> receiverIDs, String method, Map<String, Object> content) {
    Long expiration = Utils.fromNow(EPHEMERAL_MESSAGE_EXPIRATION, EXPIRATION_INTERVAL);

    Jedis client = pool.getResource();
    try {
      String message = InvokeMethodMessage.ephemeral(method, from, content, expiration).toJson();

      //use a pipeline since we are sending many messages at once
      Pipeline p = client.pipelined();
      for (String receiverID : receiverIDs) {
        //add each message to a list so a receiver can use LBPOP to block until
        //new messages appear
        String incomingMessagesKey = RedisMessageHelpers.makeIncomingMessageKey(receiverID);

        p.lpush(incomingMessagesKey, message);
      }
      p.sync();

    } catch (Exception ex) {
      LOG.warn("problem with sendEphemeralMessages({},{},{}) - {}",
              receiverIDs, method, content, Utils.getLoggableException(ex));

    } finally {
      pool.returnResource(client);
    }
  }
}
