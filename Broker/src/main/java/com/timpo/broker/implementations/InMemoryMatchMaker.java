package com.timpo.broker.implementations;

import com.google.common.collect.Sets;
import com.timpo.broker.interfaces.MatchMaker;
import com.timpo.common.Utils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class InMemoryMatchMaker implements MatchMaker {

  private final ConcurrentMap<String, Set<String>> resourceCapabilities;

  public InMemoryMatchMaker(ConcurrentMap<String, Set<String>> resourceCapabilities) {
    this.resourceCapabilities = resourceCapabilities;
  }

  public Set<String> meetRequirements(Map<String, String> requirements) {
    Set<String> matchingResources = null;

    for (Map.Entry<String, String> entry : requirements.entrySet()) {
      String capabilityType = entry.getKey();
      String specificCapability = entry.getValue();

      String key = getKey(capabilityType, specificCapability);

      Set<String> resources = resourceCapabilities.get(key);

      //if any of these sets are empty, we already know that no resource can
      //meet ALL the requirements
      if (resources == null || (matchingResources != null && matchingResources.isEmpty())) {
        return new HashSet();
      }

      //intersect the sets
      if (matchingResources == null) {
        matchingResources = resources;

      } else {
        matchingResources = Sets.intersection(resources, matchingResources);
      }
    }

    return matchingResources;
  }

  public void registerCapabilities(Map<String, List<String>> capabilities, String resourceID) {
    for (Map.Entry<String, List<String>> entry : capabilities.entrySet()) {
      String capabilityType = entry.getKey();
      List<String> specificCapabilities = entry.getValue();

      for (String specificCapability : specificCapabilities) {
        String key = getKey(capabilityType, specificCapability);

        //we want to add our resourceID to resources, but we need to make sure
        //that if the set needs to be initialized, we aren't just overwriting a
        //set that another thread just created
        Set<String> resources = resourceCapabilities.get(key);
        if (resources == null) {
          //TODO: this set should also probably come through Guice
          resourceCapabilities.putIfAbsent(key, Utils.makeConcurrentSet());
          resources = resourceCapabilities.get(key);
        }

        resources.add(resourceID);
      }
    }
  }

  public void forgetCapabilities() {
    resourceCapabilities.clear();
  }

  private String getKey(String capabilityType, String specificCapability) {
    return capabilityType + ":" + specificCapability;
  }
}
