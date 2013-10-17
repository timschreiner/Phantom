package com.timpo.broker.implementations;

import com.google.common.base.Optional;
import com.timpo.broker.interfaces.Auctioneer;
import com.timpo.common.Utils;
import com.timpo.messaging.interfaces.MessageSender;
import com.timpo.messaging.messaginginterfaces.BrokerToResource;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

public class InMemoryAuctioneer implements Auctioneer {

  private static final Logger LOG = Utils.logFor("InMemoryAuctioneer");
  //
  private final ConcurrentMap<String, PriorityQueue<Bid>> bidsForTasks;
  private final MessageSender<BrokerToResource> brokerToResourceSender;

  public InMemoryAuctioneer(
          ConcurrentMap<String, PriorityQueue<Bid>> bidsForTasks,
          MessageSender<BrokerToResource> brokerToResourceSender) {

    this.bidsForTasks = bidsForTasks;
    this.brokerToResourceSender = brokerToResourceSender;
  }

  @Override
  public void requestBids(Map<String, String> requirements, String taskID,
          Set<String> resourceIDs, long noBidsAfter, TimeUnit interval) {

    //request bids from all the resources
    brokerToResourceSender.send().requestBids(requirements, taskID, resourceIDs);

    //wait for the bids to come in
    try {
      //TODO: figure out how to handle interruptions
      Thread.sleep(TimeUnit.MILLISECONDS.convert(noBidsAfter, interval));
    } catch (InterruptedException ex) {
      LOG.warn("requestBids interrupted : {}", Utils.getLoggableException(ex));
    }
  }

  @Override
  public void trackBid(String taskID, String resourceID, double bid) {
    PriorityQueue<Bid> bids = getBidsForTask(taskID);
    if (bids == null) {
      LOG.warn("bid tracker was unavailable");
      return;
    }

    bids.add(new Bid(resourceID, bid));
  }

  @Override
  public Optional<String> nextHighBidder(String taskID) {
    PriorityQueue<Bid> bids = getBidsForTask(taskID);

    Bid bid;
    if (bids == null || bids.isEmpty() || (bid = bids.poll()) == null) {
      return Optional.absent();

    } else {
      return Optional.of(bid.getResourceID());
    }
  }

  @Override
  public void createBidsTracker(String taskID) {
    bidsForTasks.put(taskID, new PriorityQueue<Bid>());
  }

  @Override
  public void cleanupBidsTracker(String taskID) {
    bidsForTasks.remove(taskID);
  }

  private PriorityQueue<Bid> getBidsForTask(String taskID) {
    return bidsForTasks.get(taskID);
  }
}
