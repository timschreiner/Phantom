package com.timpo.messaging.implementations;

import com.timpo.common.Utils;
import com.timpo.common.models.Task;
import com.timpo.messaging.implementations.redis.RedisMessageHelpers;
import com.timpo.messaging.implementations.redis.RedisMessageSender;
import com.timpo.messaging.messaginginterfaces.CoordinatorToBroker;
import com.timpo.messaging.interfaces.MessageSender;
import java.util.Map;
import org.slf4j.Logger;
import redis.clients.jedis.JedisPool;

public class RedisSenderCoordinatorToBroker extends RedisMessageSender implements MessageSender<CoordinatorToBroker> {

  private static final Logger LOG = Utils.logFor("RedisCoordinatorToBrokerSender");
  //
  private final String brokerID;

  public RedisSenderCoordinatorToBroker(JedisPool pool, String coordinatorID, String brokerID) {
    super(pool, coordinatorID);

    this.brokerID = brokerID;
  }
  private final CoordinatorToBroker c2b = new CoordinatorToBroker() {
    public void canProcess(Task task) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("task", task)
                .build();

        sendEphemeralMessage(brokerID, "canProcess", params);

      } catch (Exception ex) {
        LOG.warn("problem sending canProcess message from {} to {} - {}", from(),
                brokerID, Utils.getLoggableException(ex));
      }
    }

    public void process(Task task) {
      try {
        Map<String, Object> params = RedisMessageHelpers.newObjectMap()
                .put("task", task)
                .build();

        sendEphemeralMessage(brokerID, "process", params);

      } catch (Exception ex) {
        LOG.warn("problem sending process message from {} to {} - {}", from(),
                brokerID, Utils.getLoggableException(ex));
      }
    }
  };

  public CoordinatorToBroker send() {
    return c2b;
  }
}
