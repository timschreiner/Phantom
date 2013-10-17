package com.timpo.broker.implementations;

import com.timpo.broker.interfaces.Foreman;
import com.timpo.common.Utils;
import com.timpo.common.models.Task;
import com.timpo.messaging.messaginginterfaces.BrokerToResource;
import com.timpo.messaging.interfaces.MessageSender;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;

public class InMemoryForeman implements Foreman {

  private static final Logger LOG = Utils.logFor("InMemoryForeman");
  //
  private static final String UNCLAIMED = "!unclaimed!";
  //
  private final MessageSender<BrokerToResource> brokerToResourceSender;
  private final Map<String, AtomicReference<String>> claimsTracker;

  public InMemoryForeman(MessageSender<BrokerToResource> brokerToResourceSender) {
    this.brokerToResourceSender = brokerToResourceSender;

    claimsTracker = new ConcurrentHashMap<String, AtomicReference<String>>();
  }

  public boolean claimTask(String taskID, String resourceID) {
    AtomicReference<String> claim = claimsTracker.get(taskID);
    if (claim == null) {
      //something's gone very wrong
      LOG.warn("claim for taskID=" + taskID + " should exist but does not");

      return false;
    }

    //returns true if we are the first resource to claim this task
    return claim.compareAndSet(resourceID, UNCLAIMED);
  }

  public void offerTask(Task task, String resourceID) {
    brokerToResourceSender.send().offer(task, resourceID);
  }

  public void assignTask(String taskID, String resourceID) {
    //make sure that the resource we're offering this to is the one that claimed it
    String claimedBy = claimsTracker.get(taskID).get();

    if (claimedBy.equals(resourceID)) {
      brokerToResourceSender.send().processTask(taskID, resourceID);

    } else {
      //TODO: what do we do here?
      LOG.warn("unable to assign taskID=" + taskID
              + " to resourceID=" + resourceID
              + " : task already claimed by resourceID=" + claimedBy);
    }
  }

  public void createClaimsTracker(String taskID) {
    claimsTracker.put(taskID, new AtomicReference<String>(UNCLAIMED));
  }

  public void cleanupClaimsTracker(String taskID) {
    claimsTracker.remove(taskID);
  }

  public boolean taskClaimed(String taskID) {
    AtomicReference<String> claim = claimsTracker.get(taskID);
    if (claim == null) {
      //something's gone very wrong
      LOG.warn("claim for taskID=" + taskID + " should exist but does not");

      return false;
    }

    return !claim.get().equals(UNCLAIMED);
  }
}
