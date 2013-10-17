package com.timpo.common.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;
import com.timpo.common.Constants.TaskStates;
import com.timpo.common.Utils;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents some unit of work
 */
public final class Task implements Delayed {

  private final String id;
  private final int priority;
  private final AtomicReference<String> currentState;
  private final AtomicLong lastUpdated;
  //NOTE: these are not concurrent because only one thread will ever be
  //manipulating or iterating through it at any time
  private final Map<String, String> requirements;
  private final Map<String, Object> params;
  private final Map<String, Set<String>> outputs;
  //creation time is only used if two tasks have equal priority, in which case
  //the older task is ranked lower (so it appears sooner in a queue)
  private final long creationTime;
  //used for delaying processing of the object
  private final AtomicLong delayedUntil;

  /**
   * @param id the id that can be used by {@link Storage} to retrieve this task
   *
   * @param priority the order the task will be processed in, highest first
   *
   * @param requirements the requirements that have to be met to process this
   * task
   *
   * @param params additional data associated with this task
   *
   * @param outputs tasks that run depending on the final state of the task
   */
  public Task(String id,
          int priority,
          Map<String, String> requirements,
          Map<String, Object> params,
          Map<String, Set<String>> outputs) {

    this.id = id;
    this.priority = priority;
    this.requirements = requirements;
    this.params = params;
    this.outputs = outputs;

    currentState = new AtomicReference<String>(TaskStates.UNTOUCHED);
    lastUpdated = new AtomicLong(Utils.currentTime());
    creationTime = Utils.currentTime();
    delayedUntil = new AtomicLong(0);
  }

  /**
   * Json compatible constructor
   *
   * @param props a map of properties that need to be manually mapped to the
   * members
   */
  @JsonCreator
  public Task(Map<String, Object> props) {
    id = (String) props.get("id");
    priority = (Integer) props.get("priority");
    requirements = (Map<String, String>) props.get("requirements");
    params = (Map<String, Object>) props.get("params");
    outputs = (Map<String, Set<String>>) props.get("outputs");
    lastUpdated = new AtomicLong((Long) props.get("lastUpdated"));

    currentState = new AtomicReference<String>(TaskStates.UNTOUCHED);
    creationTime = Utils.currentTime();
    delayedUntil = new AtomicLong(0);
  }

  /**
   * Generate a Universally Unique Task ID
   *
   * @return a new Universally Unique Task ID
   */
  public static String generateID() {
    return Utils.generateUniqueID();
  }

  /**
   * @return the id that can be used by {@link Storage} to retrieve this task
   */
  public String getID() {
    return id;
  }

  /**
   * @return the priority of this task (0 is normal priority, positive numbers
   * are higher priority and negative numbers are lower priority)
   */
  public int getPriority() {
    return priority;
  }

  /**
   * @return the output names and the IDs for tasks that should be run for that
   * output
   */
  public Map<String, Set<String>> getOutputs() {
    return outputs;
  }

  /**
   * @return the requirements that have to be met to process this task
   */
  public Map<String, String> getRequirements() {
    return requirements;
  }

  /**
   * @return the current state of the object
   */
  public String currentState() {
    return currentState.get();
  }

  /**
   * Changes the currentState of the task to this state and updates the
   * lastUpdated time to now
   *
   * @param state the new state of the task
   */
  public void updateState(String state) {
    currentState.set(state);

    lastUpdated.set(Utils.currentTime());
  }

  /**
   * @return the last time this task was modified
   */
  public long getLastUpdated() {
    return lastUpdated.get();
  }

  /**
   * @return additional data associated with this task
   */
  public Map<String, Object> getParams() {
    return params;
  }

  /**
   * Update the task's current params, combining (and in the case of duplicate
   * keys, overwriting) the params's entries
   * <br/>
   * <br/>
   * NOTE: since multiple output tasks run concurrently, there's no way to know
   * when the params is being updated, or what with. care must be taken when
   * adding a key that
   *
   * @param newParams new params to merge
   */
  //
  public synchronized void mergeParams(Map<String, Object> newParams) {
    for (Map.Entry<String, Object> entry : newParams.entrySet()) {
      params.put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * @return the names of the outputs to this task. outputs list the tasks that
   * are to be run depending on the final state of this task
   */
  @JsonIgnore
  public Set<String> outputNames() {
    return outputs.keySet();
  }

  /**
   * @param output - one of the outputs listed for this task
   * @return the TaskIDs that are associated with that output
   */
  public Optional<Set<String>> taskIDsForOutput(String output) {
    Set<String> tasksIDs = outputs.get(output);

    if (tasksIDs == null) {
      return Optional.absent();
    } else {
      return Optional.of(tasksIDs);
    }
  }

  /**
   * @param state the final state of the task (should be one of the outputs)
   * @return the TaskIDs that are associated with the output matching that state
   */
  public Optional<Set<String>> setFinalState(String state) {
    updateState(state);

    return taskIDsForOutput(state);
  }

  /**
   * Can be used to delay the processing of a task
   *
   * @param delay the number of TimeUnits to delay processing for
   * @param timeUnit
   */
  public void delayFor(long delay, TimeUnit timeUnit) {
    long currentTime = Utils.currentTime();
    long nanosecondDelay = timeUnit.toNanos(delay);

    delayedUntil.set(currentTime + nanosecondDelay);
  }

  /**
   * End any processing delay associated with this task
   */
  public void endDelay() {
    delayedUntil.set(0);
  }

  /**
   * Get the time when this task's delay is over
   *
   * @param timeUnit
   * @return time when this delay is over
   */
  @Override
  public long getDelay(TimeUnit timeUnit) {
    return delayedUntil.get();
  }

  @Override
  public int compareTo(Delayed delayed) {
    if (delayed == null) {
      return -1;

    } else {
      //we want these sorted so that the element whose delay expired furthest in
      //the past comes first
      return (int) (delayedUntil.get() - delayed.getDelay(Utils.defaultTimeUnit()));
    }
  }

  /**
   * Class that will compare tasks based on their priority and, in the case of
   * ties, how long they've been in the processing cycle
   */
  public final static class PriorityComparator implements Comparator<Task> {

    public int compare(Task t1, Task t2) {
      //never call this with null arguments

      //we want to sort highest priority first, so this comparison is backwards
      int priorityDiff = t2.priority - t1.priority;
      if (priorityDiff != 0) {
        return priorityDiff;
      }

      //we also want the oldest tasks to have 'seniority' over tasks with equal
      //priority
      int creationTimeDiff = (int) (t1.creationTime - t2.creationTime);
      if (creationTimeDiff != 0) {
        return creationTimeDiff;
      }

      //finally, just compare the id's
      return t1.id.compareTo(t2.id);
    }
  }

  //<editor-fold defaultstate="collapsed" desc="generated-code">
  /**
   *
   * @return
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 97 * hash + this.priority;
    hash = 97 * hash + (this.currentState != null ? this.currentState.hashCode() : 0);
    hash = 97 * hash + (this.requirements != null ? this.requirements.hashCode() : 0);
    hash = 97 * hash + (this.params != null ? this.params.hashCode() : 0);
    hash = 97 * hash + (this.outputs != null ? this.outputs.hashCode() : 0);
    return hash;
  }

  /**
   *
   * @param obj
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Task other = (Task) obj;
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    if (this.priority != other.priority) {
      return false;
    }
    if (this.currentState != other.currentState && (this.currentState == null || !this.currentState.equals(other.currentState))) {
      return false;
    }
    if (this.requirements != other.requirements && (this.requirements == null || !this.requirements.equals(other.requirements))) {
      return false;
    }
    if (this.params != other.params && (this.params == null || !this.params.equals(other.params))) {
      return false;
    }
    if (this.outputs != other.outputs && (this.outputs == null || !this.outputs.equals(other.outputs))) {
      return false;
    }
    return true;
  }

  /**
   *
   * @return
   */
  @Override
  public String toString() {
    return "Task{" + "id=" + id + ", priority=" + priority + ", currentState=" + currentState + ", requirements=" + requirements + ", params=" + params + ", outputs=" + outputs + '}';
  }
  //</editor-fold>
}
