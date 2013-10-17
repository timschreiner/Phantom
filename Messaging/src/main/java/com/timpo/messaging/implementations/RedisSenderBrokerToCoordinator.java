package com.timpo.messaging.implementations;

import com.timpo.common.Utils;
import com.timpo.messaging.implementations.redis.RedisMessageHelpers;
import com.timpo.messaging.implementations.redis.RedisMessageSender;
import com.timpo.messaging.interfaces.MessageSender;
import com.timpo.messaging.messaginginterfaces.BrokerToCoordinator;
import java.util.Map;
import org.slf4j.Logger;
import redis.clients.jedis.JedisPool;

public class RedisSenderBrokerToCoordinator extends RedisMessageSender implements MessageSender<BrokerToCoordinator> {

  private static final Logger LOG = Utils.logFor("RedisBrokerToCoordinatorSender");
  //
  private final String TO;

  public RedisSenderBrokerToCoordinator(JedisPool pool, String brokerID, String coordinatorID) {
    super(pool, brokerID);

    TO = coordinatorID;
  }
  private final BrokerToCoordinator b2c = new BrokerToCoordinator() {
    public void resourcesAvailable(String taskID) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("taskID", taskID)
                .build();

        sendEphemeralMessage(TO, "resourcesAvailable", params);

      } catch (Exception ex) {
        LOG.warn("problem sending resourcesAvailable message from {} to {} - {}", from(),
                TO, Utils.getLoggableException(ex));
      }
    }

    public void resourcesUnavailable(String taskID) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("taskID", taskID)
                .build();

        sendEphemeralMessage(TO, "resourcesUnavailable", params);

      } catch (Exception ex) {
        LOG.warn("problem sending resourcesUnavailable message from {} to {} - {}", from(),
                TO, Utils.getLoggableException(ex));
      }
    }

    public void processingTask(String taskID, String resourceID) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("taskID", taskID)
                .put("resourceID", resourceID)
                .build();

        sendEphemeralMessage(TO, "processingTask", params);

      } catch (Exception ex) {
        LOG.warn("problem sending processingTask message from {} to {} - {}", from(),
                TO, Utils.getLoggableException(ex));
      }
    }

    public void taskFinished(String taskID, String newState, Map<String, Object> newParams) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("taskID", taskID)
                .put("newState", newState)
                .put("newParams", newParams)
                .build();

        sendDurableMessage(TO, "taskFinished", params);

      } catch (Exception ex) {
        LOG.warn("problem sending taskFinished message from {} to {} - {}", from(),
                TO, Utils.getLoggableException(ex));
      }
    }
  };

  public BrokerToCoordinator send() {
    return b2c;
  }
}
