package com.timpo.broker.interfaces;

import java.util.Set;

/**
 * Tracks the availability of resources
 */
public interface ResourceTracker {

  /**
   * Mark a resource as being available for handling tasks
   *
   * @param resourceID
   */
  public void markAvailable(String resourceID);

  /**
   * Mark a resource as being unavailable for handling tasks
   *
   * @param resourceID
   */
  public void markBusy(String resourceID);

  /**
   * Find the resources within a set of ResourceIDs that are not currently busy
   *
   * @param resourceIDs
   *
   * @return IDs for the available resources
   */
  public Set<String> findAvailable(Set<String> resourceIDs);
}
