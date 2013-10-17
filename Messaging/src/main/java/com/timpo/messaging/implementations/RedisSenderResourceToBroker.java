package com.timpo.messaging.implementations;

import com.timpo.common.Utils;
import com.timpo.messaging.implementations.redis.RedisMessageHelpers;
import com.timpo.messaging.implementations.redis.RedisMessageSender;
import com.timpo.messaging.interfaces.MessageSender;
import com.timpo.messaging.messaginginterfaces.ResourceToBroker;
import java.util.Map;
import org.slf4j.Logger;
import redis.clients.jedis.JedisPool;

public class RedisSenderResourceToBroker extends RedisMessageSender implements MessageSender<ResourceToBroker> {

  private static final Logger LOG = Utils.logFor("RedisBrokerToResourceSender");
  private final String brokerID;

  public RedisSenderResourceToBroker(JedisPool pool, String from, String brokerID) {
    super(pool, from);
    this.brokerID = brokerID;
  }

  private final ResourceToBroker r2b = new ResourceToBroker() {
    public void placeBid(String taskID, String resourceID, double bid) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("taskID", taskID)
                .put("resourceID", resourceID)
                .put("bid", bid)
                .build();

        sendEphemeralMessage(brokerID, "placeBid", params);

      } catch (Exception ex) {
        LOG.warn("problem sending placeBid message from {} to {} - {}", from(),
                resourceID, Utils.getLoggableException(ex));
      }
    }

    public void claimTask(String taskID, String resourceID) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("taskID", taskID)
                .put("resourceID", resourceID)
                .build();

        sendEphemeralMessage(brokerID, "claimTask", params);

      } catch (Exception ex) {
        LOG.warn("problem sending claimTask message from {} to {} - {}", from(),
                resourceID, Utils.getLoggableException(ex));
      }
    }

    public void processingTask(String taskID, String resourceID) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("taskID", taskID)
                .put("resourceID", resourceID)
                .build();

        sendEphemeralMessage(brokerID, "processingTask", params);

      } catch (Exception ex) {
        LOG.warn("problem sending processingTask message from {} to {} - {}", from(),
                resourceID, Utils.getLoggableException(ex));
      }
    }

    public void finishedProcessing(String taskID, String resourceID, String newState, Map<String, Object> newParams) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("taskID", taskID)
                .put("resourceID", resourceID)
                .put("newState", newState)
                .put("newParams", newParams)
                .build();

        sendDurableMessage(brokerID, "finishedProcessing", params);

      } catch (Exception ex) {
        LOG.warn("problem sending finishedProcessing message from {} to {} - {}", from(),
                resourceID, Utils.getLoggableException(ex));
      }
    }
  };

  public ResourceToBroker send() {
    return r2b;
  }
}
