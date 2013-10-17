package com.timpo.broker.interfaces;

import com.google.common.base.Optional;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Assigns tasks to Resources based on what they can offer and confirms whether
 * the assignment was successful or not
 */
public interface Auctioneer {

  /**
   * Requests that a set of Resources bid on a task based on its requirements.
   * The request will wait until (noBidsAfter * interval) and then commence
   * attempting to assign work to the highest bidders.
   *
   *
   * @param requirements the requirements that the Resource will calculate a bid
   * for
   *
   * @param taskID the task we want to get bids for
   *
   * @param resourceIDs the Resources we want to get offers from
   *
   * @param noBidsAfter check for responses after waiting for (noBidsAfter *
   * interval)
   *
   * @intreval Unit of time noBidsAfter refers to
   */
  public void requestBids(Map<String, String> requirements, String taskID,
          Set<String> resourceIDs, long noBidsAfter, TimeUnit interval);

  /**
   * Tracks a resource's bid for a particular task
   *
   * @param taskID the Task being bid on
   *
   * @param ResourceID the resource making the bid
   *
   * @param bid a score calculated by the resource. higher is better
   */
  public void trackBid(String taskID, String ResourceID, double bid);

  /**
   * Gets and removes the highest bidding Resource for a task
   *
   * @param taskID the task the bids were made for
   *
   * @return the highest bidder for the task (which may or may not exist)
   */
  public Optional<String> nextHighBidder(String taskID);

  /**
   * Create a place where the highest bids can be tracked
   *
   * @param taskID the bid tracker to create
   */
  public void createBidsTracker(String taskID);

  /**
   * Reclaim the memory being used by the bids tracker
   *
   * @param taskID the bid tracker to delete
   */
  public void cleanupBidsTracker(String taskID);

  /**
   * Stores bids and sorts highest-bid-first
   */
  public static class Bid implements Comparable<Bid> {

    private final String resourceID;
    private final double bid;

    public int compareTo(Bid t) {
      //we want to sort by highest bid first, so we compare backwards
      int bidDiff = (int) (t.bid - bid);
      if (bidDiff != 0) {
        return bidDiff;
      }

      return resourceID.compareTo(t.resourceID);
    }

    //<editor-fold defaultstate="collapsed" desc="generated">
    public Bid(String resourceID, double bid) {
      this.resourceID = resourceID;
      this.bid = bid;
    }

    public double getBid() {
      return bid;
    }

    public String getResourceID() {
      return resourceID;
    }

    @Override
    public String toString() {
      return "Bid{" + "resourceID=" + resourceID + ", bid=" + bid + '}';
    }

    @Override
    public int hashCode() {
      int hash = 3;
      hash = 97 * hash + (this.resourceID != null ? this.resourceID.hashCode() : 0);
      hash = 97 * hash + (int) (Double.doubleToLongBits(this.bid) ^ (Double.doubleToLongBits(this.bid) >>> 32));
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final Bid other = (Bid) obj;
      if ((this.resourceID == null) ? (other.resourceID != null) : !this.resourceID.equals(other.resourceID)) {
        return false;
      }
      if (Double.doubleToLongBits(this.bid) != Double.doubleToLongBits(other.bid)) {
        return false;
      }
      return true;
    }
    //</editor-fold>
  }
}
