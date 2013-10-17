package com.timpo.broker.implementations;

import com.google.common.collect.Sets;
import com.timpo.broker.interfaces.ResourceTracker;
import java.util.Set;

public class InMemoryResourceTracker implements ResourceTracker {

  Set<String> busyResources;

  public InMemoryResourceTracker(Set<String> busyResources) {
    this.busyResources = busyResources;
  }

  public void markAvailable(String resourceID) {
    busyResources.remove(resourceID);
  }

  public void markBusy(String resourceID) {
    busyResources.add(resourceID);
  }

  public Set<String> findAvailable(Set<String> resourceIDs) {
    return Sets.difference(resourceIDs, busyResources);
  }
}
