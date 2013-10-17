
import com.timpo.common.Constants;
import com.timpo.common.Utils;
import com.timpo.messaging.implementations.RedisReceiverCoordinator;
import com.timpo.messaging.implementations.RedisSenderBrokerToCoordinator;
import com.timpo.messaging.interfaces.MessageReceiver;
import com.timpo.messaging.interfaces.MessageSender;
import com.timpo.messaging.messaginginterfaces.BrokerToCoordinator;
import com.timpo.messaging.messaginginterfaces.ToCoordinatorMessages;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import redis.clients.jedis.JedisPool;

public class Idea {

  public static void main(String[] args) throws Exception {
    String coordinatorID = Constants.Defaults.COORDINATOR_ID;
    String brokerID = Constants.Defaults.BROKER_ID;
    JedisPool pool = new JedisPool("localhost");
    MessageSender<BrokerToCoordinator> brokerMessageSender = new RedisSenderBrokerToCoordinator(pool, brokerID, coordinatorID);

    ToCoordinatorMessages handler = new ToCoordinatorMessages() {
      private void debug(String message) {
        System.out.println("message received: " + message);
      }

      public void incomingJobRequest(String stringJob) {
        debug("incomingJobRequest");
      }

      public void acceptIncomingJobs(boolean accept) {
        debug("acceptIncomingJobs");
      }

      public void resourcesAvailable(String taskID) {
        debug("resourcesAvailable");
      }

      public void resourcesUnavailable(String taskID) {
        debug("resourcesUnavailable");
      }

      public void processingTask(String taskID, String resourceID) {
        debug("processingTask");
      }

      public void taskFinished(String taskID, String newState, Map<String, Object> newParams) {
        debug("taskFinished");
      }
    };

    MessageReceiver<ToCoordinatorMessages> coordinatorMessageReceiver = new RedisReceiverCoordinator(pool, coordinatorID, handler);

    try {
      brokerMessageSender.start();
      coordinatorMessageReceiver.start();

      brokerMessageSender.send().processingTask("taskID", "resourceID");

      Utils.sleep(5, TimeUnit.SECONDS);

    } finally {
      brokerMessageSender.stop();
      coordinatorMessageReceiver.stop();
    }

  }
}
