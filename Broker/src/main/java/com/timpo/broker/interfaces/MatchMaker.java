package com.timpo.broker.interfaces;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Find the resources with the capabilities to handle a given task's
 * requirements
 */
public interface MatchMaker {

  /**
   * Finds the IDs for resources capable of handling some set of requirements.
   * <br/>
   * <br/>
   * NOTE: any specific requirement of 'SCORE' will not be tracked, as it is
   * used by scorekeeper to find the best resource for a task.
   *
   * @param requirements one or more capabilities a resource has to handle in
   * order for it to perform some task. The key is the general type of
   * requirement and the value is the specific requirement
   *
   * @return the set of resourceIDs that meet the supplied requirements
   */
  public Set<String> meetRequirements(Map<String, String> requirements);

  /**
   * Notifies MatchMaker that a particular resource can handle specific
   * requirements
   *
   * @param capabilities all the requirements that this resource is capable of
   * processing
   *
   * @param resourceID the ID of the resource that can handle this requirement
   */
  public void registerCapabilities(Map<String, List<String>> capabilities, String resourceID);

  /**
   * Removes all previously registered requirements. Should be called
   * occasionally to clear out capabilities that are no longer true.
   */
  public void forgetCapabilities();
}
