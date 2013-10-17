package com.timpo.messaging.implementations;

import com.timpo.common.Utils;
import com.timpo.common.models.Task;
import com.timpo.messaging.implementations.redis.RedisMessageHelpers;
import com.timpo.messaging.implementations.redis.RedisMessageSender;
import com.timpo.messaging.interfaces.MessageSender;
import com.timpo.messaging.messaginginterfaces.BrokerToResource;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import redis.clients.jedis.JedisPool;

public class RedisSenderBrokerToResource extends RedisMessageSender implements MessageSender<BrokerToResource> {

  private static final Logger LOG = Utils.logFor("RedisBrokerToResourceSender");

  public RedisSenderBrokerToResource(JedisPool pool, String from) {
    super(pool, from);
  }
  private final BrokerToResource b2r = new BrokerToResource() {
    public void requestBids(Map<String, String> requirements, String taskID, Set<String> resourceIDs) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("requirements", requirements)
                .put("taskID", taskID)
                .build();

        sendEphemeralMessages(resourceIDs, "requestBids", params);

      } catch (Exception ex) {
        LOG.warn("problem sending requestBids message from {} to {} - {}", from(),
                resourceIDs, Utils.getLoggableException(ex));
      }
    }

    public void offer(Task task, String resourceID) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("task", task)
                .build();

        sendEphemeralMessage(resourceID, "offer", params);

      } catch (Exception ex) {
        LOG.warn("problem sending offer message from {} to {} - {}", from(),
                resourceID, Utils.getLoggableException(ex));
      }
    }

    public void processTask(String taskID, String resourceID) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("taskID", taskID)
                .put("resourceID", resourceID)
                .build();

        sendDurableMessage(resourceID, "processTask", params);

      } catch (Exception ex) {
        LOG.warn("problem sending processTask message from {} to {} - {}", from(),
                resourceID, Utils.getLoggableException(ex));
      }
    }

    public void cancelOffer(String taskID, String resourceID) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("taskID", taskID)
                .put("resourceID", resourceID)
                .build();

        sendDurableMessage(resourceID, "cancelOffer", params);

      } catch (Exception ex) {
        LOG.warn("problem sending cancelOffer message from {} to {} - {}", from(),
                resourceID, Utils.getLoggableException(ex));
      }
    }
  };

  public BrokerToResource send() {
    return b2r;
  }
}
