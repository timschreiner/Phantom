package com.timpo.common.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.timpo.common.Utils;
import java.util.List;

/**
 * A Job is just a list of {@link Task}s with an associated ID
 */
public class Job {

  private final String id;
  private final List<Task> tasks;

  /**
   * Generate a Universally Unique Task ID
   *
   * @return a new Universally Unique Task ID
   */
  public static String generateID() {
    return Utils.generateUniqueID();
  }

  /**
   * Gets the root task of the job
   *
   * @return the root task of the job
   */
  public Task getRootTask() {
    return tasks.get(0);
  }

  /**
   * Create a new instance of Job
   *
   * @param id
   * @param tasks
   */
  @JsonCreator
  public Job(
          @JsonProperty("id") String id,
          @JsonProperty("tasks") List<Task> tasks) {
    this.id = id;
    this.tasks = tasks;
  }

  //<editor-fold defaultstate="collapsed" desc="generated-code">
  /**
   * Get list of the {@link Task}s for this job. The first task should always be
   * the root task
   *
   * @return {@link List} of {@link Task}s
   */
  public List<Task> getTasks() {
    return tasks;
  }

  /**
   * Get the id associated with this job
   *
   * @return the id associated with this job
   */
  public String getId() {
    return id;
  }

  /**
   * Get a string representation of this job
   *
   * @return string representation of this job
   */
  @Override
  public String toString() {
    return "Job{" + "id=" + id + ", tasks=" + tasks + '}';
  }
  //</editor-fold>
}
