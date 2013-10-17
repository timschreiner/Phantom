package com.timpo.broker.logic;

import com.google.common.base.Optional;
import com.timpo.broker.interfaces.Auctioneer;
import com.timpo.broker.interfaces.Foreman;
import com.timpo.broker.interfaces.MatchMaker;
import com.timpo.broker.interfaces.ResourceTracker;
import com.timpo.common.Utils;
import com.timpo.common.models.Task;
import com.timpo.messaging.messaginginterfaces.ToBrokerMessages;
import com.timpo.messaging.messaginginterfaces.BrokerToCoordinator;
import com.timpo.messaging.messaginginterfaces.BrokerToResource;
import com.timpo.messaging.interfaces.MessageSender;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

public class BrokerMessageHandler implements ToBrokerMessages {

  private static final Logger LOG = Utils.logFor("CoordinatorRequestHandler");
  //
  private static final long BIDDING_DURATION = 5;
  private static final TimeUnit BIDDING_INTERVAL = TimeUnit.SECONDS;
  private static final int CHECK_ATTEMPTS = 5;
  private static final TimeUnit CHECK_INTERVAL = TimeUnit.SECONDS;
  //
  private final MatchMaker matchMaker;
  private final ResourceTracker resourceTracker;
  private final Auctioneer auctioneer;
  private final Foreman foreman;
  private final MessageSender<BrokerToCoordinator> brokerToCoordinatorSender;
  private final MessageSender<BrokerToResource> brokerToResourceSender;

  public BrokerMessageHandler(MatchMaker matchMaker,
          ResourceTracker resourceTracker, Auctioneer auctioneer, Foreman foreman,
          MessageSender<BrokerToCoordinator> brokerToCoordinatorSender,
          MessageSender<BrokerToResource> brokerToResourceSender) {

    this.matchMaker = matchMaker;
    this.resourceTracker = resourceTracker;
    this.auctioneer = auctioneer;
    this.foreman = foreman;
    this.brokerToCoordinatorSender = brokerToCoordinatorSender;
    this.brokerToResourceSender = brokerToResourceSender;
  }

  public void canProcess(Task task) {
    String taskID = task.getID();

    Set<String> availableResources = findAvailableResources(task);
    if (availableResources.isEmpty()) {
      brokerToCoordinatorSender.send().resourcesUnavailable(taskID);

    } else {
      brokerToCoordinatorSender.send().resourcesAvailable(taskID);
    }
  }

  public void process(Task task) {
    String taskID = task.getID();
    Set<String> availableResources = findAvailableResources(task);
    if (availableResources.isEmpty()) {
      //we can't process this task
      brokerToCoordinatorSender.send().resourcesUnavailable(taskID);
      return;
    }

    foreman.createClaimsTracker(taskID);

    //get quotes on how well each resource thinks it can handle the job
    auctioneer.requestBids(task.getRequirements(), taskID, availableResources,
            BIDDING_DURATION, BIDDING_INTERVAL);

    //now that enough time has passed, grab the highest bidder
    while (true) {
      Optional<String> highBidder = auctioneer.nextHighBidder(taskID);

      //if there are no high bidders, this job is unprocessable
      if (!highBidder.isPresent()) {
        brokerToCoordinatorSender.send().resourcesUnavailable(taskID);
        break;
      }

      String resourceID = highBidder.get();

      //offer this task to the resource
      foreman.offerTask(task, resourceID);

      //give the resource a chance to claim that task
      for (int attempt = 0; attempt < CHECK_ATTEMPTS; attempt++) {
        try {
          Utils.sleep(1, CHECK_INTERVAL);
        } catch (InterruptedException ex) {
          LOG.warn("interrupted while attempting to assign taskID=" + taskID
                  + " to resourceID=" + resourceID);
        }

        if (foreman.taskClaimed(taskID)) {
          //a process message has already been sent to the resource, we're
          //done here
          break;
        }
      }
    }

    //if we've offered the task to everyone and noone responded, then resources are unavailable
    if (!foreman.taskClaimed(taskID)) {
      brokerToCoordinatorSender.send().resourcesUnavailable(taskID);
    }

    foreman.cleanupClaimsTracker(taskID);
  }

  private Set<String> findAvailableResources(Task task) {
    Set<String> capableResources = matchMaker.meetRequirements(task.getRequirements());
    //if there are no capable resources, there's no reason to look for available
    //resources either
    if (capableResources.isEmpty()) {
      return capableResources;
    }

    Set<String> availableResources = resourceTracker.findAvailable(capableResources);

    return availableResources;
  }

  public void placeBid(String taskID, String resourceID, double bid) {
    auctioneer.trackBid(taskID, resourceID, bid);
  }

  public void processingTask(String taskID, String resourceID) {
    //forward this information to the broker
    brokerToCoordinatorSender.send().processingTask(taskID, resourceID);

    //lock the resource
    resourceTracker.markBusy(resourceID);
  }

  public void finishedProcessing(String taskID, String resourceID, String newState, Map<String, Object> newParams) {
    //forward this information to the broker
    brokerToCoordinatorSender.send().taskFinished(taskID, newState, newParams);

    //free the resource
    resourceTracker.markAvailable(resourceID);
  }

  public void claimTask(String taskID, String resourceID) {
    if (foreman.claimTask(taskID, resourceID)) {
      //TODO: how do we make sure this was actually claimed?
      brokerToResourceSender.send().processTask(taskID, resourceID);

    } else {
      brokerToResourceSender.send().cancelOffer(taskID, resourceID);
    }
  }
}
