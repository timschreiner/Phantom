package com.timpo.coordinator;

import com.timpo.common.Constants;
import com.timpo.common.Constants.TaskStates;
import com.timpo.common.Utils;
import com.timpo.common.models.Job;
import com.timpo.common.models.Task;
import com.timpo.coordinator.implementations.ConcurrentTaskQueue;
import com.timpo.coordinator.implementations.JobValidator;
import com.timpo.coordinator.implementations.JsonJobTranslator;
import com.timpo.coordinator.implementations.JsonTaskTranslator;
import com.timpo.coordinator.implementations.RedisStorage;
import com.timpo.coordinator.interfaces.Coordinator;
import com.timpo.coordinator.interfaces.Storage;
import com.timpo.coordinator.interfaces.TaskQueue;
import com.timpo.coordinator.interfaces.Translator;
import com.timpo.coordinator.interfaces.Validator;
import com.timpo.coordinator.logic.CoordinatorMessageHandler;
import com.timpo.coordinator.logic.QueueWorker;
import com.timpo.messaging.implementations.RedisReceiverCoordinator;
import com.timpo.messaging.implementations.RedisSenderCoordinatorToBroker;
import com.timpo.messaging.messaginginterfaces.ToCoordinatorMessages;
import com.timpo.messaging.messaginginterfaces.CoordinatorToBroker;
import com.timpo.messaging.interfaces.MessageReceiver;
import com.timpo.messaging.interfaces.MessageSender;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class DefaultCoordinator implements Coordinator {

  private static final Logger LOG = Utils.logFor("CoordinatorContainer");
  //
  private final Translator<Job> translator;
  private final Validator<Job> validator;
  private final Storage storage;
  private final TaskQueue taskQueue;
  private final MessageReceiver<ToCoordinatorMessages> coordinatorReceiver;
  private final MessageSender<CoordinatorToBroker> coordinatorToBrokerSender;
  private final ExecutorService queueWorkers;

  public DefaultCoordinator(int queueSize, int numQueueWorkers,
          String redisHostname) {

    String coordinatorID = Constants.Defaults.COORDINATOR_ID;
    String brokerID = Constants.Defaults.BROKER_ID;

    //NOTE: for now this manual wiring is fine, but ultimately this feels like
    //something Guice would be better suited for

    //wire up the interfaces
    translator = new JsonJobTranslator();


    validator = new JobValidator();


    JedisPool redisPool = new JedisPool(redisHostname);
    Translator<Task> tasktrans = new JsonTaskTranslator();
    storage = new RedisStorage(redisPool, tasktrans);


    Comparator<Task> priorityComparer = new Task.PriorityComparator();
    PriorityBlockingQueue<Task> workQueue =
            new PriorityBlockingQueue<Task>(queueSize, priorityComparer);
    DelayQueue<Task> delayQueue = new DelayQueue<Task>();
    taskQueue = new ConcurrentTaskQueue(workQueue, delayQueue);


    coordinatorToBrokerSender = new RedisSenderCoordinatorToBroker(redisPool,
            coordinatorID, brokerID);


    ToCoordinatorMessages coordinatorMessageHandler =
            new CoordinatorMessageHandler(translator, validator, storage,
            taskQueue, coordinatorToBrokerSender);
    coordinatorReceiver = new RedisReceiverCoordinator(redisPool, coordinatorID,
            coordinatorMessageHandler);


    queueWorkers = Executors.newFixedThreadPool(numQueueWorkers);


    //start up a message receiver that will listen for messages to the coordinator
    coordinatorReceiver.start();

    //start up the threads that will process the task queues
    for (int i = 0; i < numQueueWorkers; i++) {
      QueueWorker qw = new QueueWorker(taskQueue, coordinatorToBrokerSender);
      queueWorkers.execute(qw);
    }
  }

  /**
   * In the event of an emergency shutdown where tasks are still being
   * processed, we can use the task data in redis to restart and repopulate the
   * processing queue
   *
   * @param redisHostname the redis instance that contains the tasks
   * @return the number of tasks restarted
   */
  public int hotRestart(String redisHostname) {
    Map<String, String> tasks = null;

    Translator<Task> tasktrans = new JsonTaskTranslator();

    Jedis client = new Jedis(redisHostname);
    try {
      tasks = client.hgetAll(RedisStorage.getTaskMapKey());

    } finally {
      client.disconnect();
    }

    int tasksRestarted = 0;
    if (tasks != null) {
      for (Map.Entry<String, String> entry : tasks.entrySet()) {
        String jsonTask = entry.getValue();
        try {
          Task task = tasktrans.decode(jsonTask);

          if (task.currentState().equals(TaskStates.WAITING_FOR_RESOURCES)) {
            taskQueue.process(task);

            tasksRestarted++;
          }

        } catch (DecoderException ex) {
          LOG.warn("unable to decode task: {} - {}", jsonTask,
                  Utils.getLoggableException(ex));
        }
      }

      return tasksRestarted;

    } else {
      return -1;
    }
  }

  //TODO: add a background thread that iterates through the tasks periodically,
  //looking for tasks that somehow got hung up in the processing loop
  @Override
  public void shutdown() {
    //TODO: what happens when this is called? can shutdown be more orderly?
    //should we empty the queues into a log file so they can be restarted?
    //should the task store use redis instead of in memory?
    queueWorkers.shutdown();
  }

  @Override
  public Translator<Job> getTranslator() {
    return translator;
  }

  @Override
  public Validator<Job> getValidator() {
    return validator;
  }

  @Override
  public Storage getStorage() {
    return storage;
  }

  @Override
  public TaskQueue getTaskQueue() {
    return taskQueue;
  }

  @Override
  public MessageReceiver<ToCoordinatorMessages> getCoordinatorReceiver() {
    return coordinatorReceiver;
  }

  @Override
  public MessageSender<CoordinatorToBroker> getCoordinatorToBrokerSender() {
    return coordinatorToBrokerSender;
  }
}
