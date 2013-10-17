package com.timpo.coordinator.logic;

import com.google.common.base.Optional;
import com.timpo.common.Constants;
import com.timpo.common.Utils;
import com.timpo.common.models.Job;
import com.timpo.common.models.Task;
import com.timpo.coordinator.interfaces.Storage;
import com.timpo.coordinator.interfaces.TaskQueue;
import com.timpo.coordinator.interfaces.Translator;
import com.timpo.coordinator.interfaces.Validator;
import com.timpo.messaging.messaginginterfaces.ToCoordinatorMessages;
import com.timpo.messaging.messaginginterfaces.CoordinatorToBroker;
import com.timpo.messaging.interfaces.MessageSender;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.ValidationException;
import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;

public class CoordinatorMessageHandler implements ToCoordinatorMessages {

  private static final Logger LOG = Utils.logFor("BrokerWorker");
  //
  private static final long PROCESSING_DELAY = 5;
  private static final TimeUnit DELAY_UNIT = TimeUnit.SECONDS;
  //
  private final Translator<Job> translator;
  private final Validator<Job> validator;
  private final Storage storage;
  private final TaskQueue taskQueue;
  private final MessageSender<CoordinatorToBroker> senderCoordinatorToBroker;
  private final AtomicBoolean acceptingJobs;

  public CoordinatorMessageHandler(Translator<Job> translator,
          Validator<Job> validator, Storage storage, TaskQueue taskQueue,
          MessageSender<CoordinatorToBroker> senderCoordinatorToBroker) {

    this.translator = translator;
    this.validator = validator;
    this.storage = storage;
    this.taskQueue = taskQueue;
    this.senderCoordinatorToBroker = senderCoordinatorToBroker;

    this.acceptingJobs = new AtomicBoolean(true);
  }

  public void resourcesAvailable(String taskID) {
    //TODO: is this the behavior we want?
    Optional<Task> task = storage.retrieveTask(taskID);
    if (task.isPresent()) {
      senderCoordinatorToBroker.send().process(task.get());

    } else {
      LOG.warn("unable to find task for canProcess(taskID=" + taskID + ")");
    }
  }

  public void resourcesUnavailable(String taskID) {
    Optional<Task> task = storage.retrieveTask(taskID);
    if (task.isPresent()) {
      boolean taskDelayed = taskQueue.delayProcessing(task.get(), PROCESSING_DELAY, DELAY_UNIT);

      if (!taskDelayed) {
        LOG.warn("unable to delay processing of task in cannotProcess(taskID=" + taskID + ")");
      }

    } else {
      LOG.warn("unable to find task for cannotProcess(taskID=" + taskID + ")");
    }
  }

  public void processingTask(String taskID, String resourceID) {
    Optional<Task> task = storage.retrieveTask(taskID);
    if (task.isPresent()) {
      //TODO: is this the behavior we want?
      Task processingTask = task.get();

      processingTask.updateState(Constants.TaskStates.PROCESSING + ":" + resourceID);

      storage.commitTask(processingTask);

    } else {
      LOG.warn("unable to find task for cannotProcess(taskID=" + taskID + ")");
    }
  }

  public void taskFinished(String taskID, String newState, Map<String, Object> newParams) {
    Optional<Task> parentTask = storage.retrieveTask(taskID);
    if (parentTask.isPresent()) {
      Task finishedTask = parentTask.get();

      //update the finished task's state
      finishedTask.setFinalState(newState);

      storage.commitTask(finishedTask);

      Optional<Set<String>> outputIDs = finishedTask.taskIDsForOutput(newState);
      if (outputIDs.isPresent()) {
        //loop through all the tasks specified in the finished task's output
        for (String outputID : outputIDs.get()) {
          Optional<Task> childTask = storage.retrieveTask(outputID);
          if (childTask.isPresent()) {
            //update and start the new tasks
            Task nextTask = childTask.get();

            nextTask.mergeParams(newParams);

            nextTask.updateState(Constants.TaskStates.WAITING_FOR_RESOURCES);

            storage.commitTask(nextTask);

            taskQueue.process(nextTask);

          } else {
            LOG.warn("unable to find output task for taskFinished(taskID=" + outputID + ")");
          }
        }
      } else {
        //TODO: do we do something else when an output is null?
        LOG.warn("there are no outputs in taskID=" + taskID + " for state=" + newState);
      }
    } else {
      LOG.warn("unable to find task for taskFinished(taskID=" + taskID + ")");
    }
  }

  @Override
  public void incomingJobRequest(String stringJob) {
    if (acceptingJobs.get()) {
      try {
        //decode the job
        Job job = translator.decode(stringJob);

        //make sure it's not going to cause weird problems
        validator.validate(job);

        //store it for future reference
        storage.trackJob(job);

        //get the first task and set it to processing
        Task rootTask = job.getRootTask();
        rootTask.updateState(Constants.TaskStates.WAITING_FOR_RESOURCES);

        if (!taskQueue.process(rootTask)) {
          LOG.warn("processing queue full for job:{}", stringJob);
        }

      } catch (DecoderException ex) {
        //TODO: make sure this format works
        LOG.warn("unable to decode stringJob:{} - {}", stringJob, Utils.getLoggableException(ex));
      } catch (ValidationException ex) {
        //TODO: make sure this format works
        LOG.warn("unable to validate job stringJob:{} - {}", stringJob, Utils.getLoggableException(ex));
      } catch (Exception ex) {
        LOG.warn("unable to handle job stringJob:{} - {}", stringJob, Utils.getLoggableException(ex));
      }
    }
  }

  public void acceptIncomingJobs(boolean accept) {
    acceptingJobs.set(accept);
  }
}
