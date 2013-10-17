package com.timpo.coordinator.interfaces;

import com.google.common.base.Optional;
import com.timpo.common.models.Task;
import com.timpo.common.models.Job;

/**
 * Used to track the progress of a {@link Job} and for finding {@link Task}s
 * when all you have is the task's ID
 */
public abstract class Storage {

  /**
   *
   * @param job
   */
  public abstract void trackJob(Job job);

  /**
   * Get a {@link Job} associated with this ID.
   *
   * @param jobID
   *
   * @return an Optional containing the {@link Job} associated with this ID if
   * it exists
   */
  public abstract Optional<Job> getJob(String jobID);

  /**
   * Remove this job from storage
   *
   * @param jobID the id for the job to be deleted
   *
   * @return true if the job was deleted, false if the job could not be found
   */
  public abstract boolean deleteJob(String jobID);

  /**
   * Get a {@link Task} associated with this ID.
   * <br/>
   * <br/>
   * NOTE: Modifications are only guaranteed to be reflected in storage if
   * commitTask() is called after updating a task
   *
   * @param taskID
   *
   * @return an Optional containing the {@link Task} associated with this ID if
   * it exists
   */
  public abstract Optional<Task> retrieveTask(String taskID);

  /**
   * Return a checked-out {@link Task} after making any updates.
   *
   * NOTE: Modifications are only guaranteed to be reflected in storage if
   * commitTask() is called after updating a task
   *
   * @param taskID
   */
  public abstract void commitTask(Task task);

  /**
   * Remove this task from storage
   *
   * @param taskID the id for the task to be deleted
   *
   * @return true if the task was deleted, false if the task could not be found
   */
  public abstract boolean deleteTask(String taskID);

  /**
   * Recursively finds and deletes this task and all its child tasks.
   *
   * @param rootTaskID the id for the task to delete
   */
  private void recursivelyDeleteChildren(String rootTaskID) {
    Task root = retrieveTask(rootTaskID).get();

    for (String output : root.outputNames()) {
      for (String childID : root.taskIDsForOutput(output).get()) {
        deleteTask(childID);

        recursivelyDeleteChildren(childID);
      }
    }
  }

  /**
   * Deletes all the 'children' of a task except those belonging to the
   * specified output. Useful when updating a task, since all the other outputs
   * are now unreachable/
   *
   * @param outputToSpare the only output that wont be recursively deleted
   * @param task the task containing the outputs to cleanup
   */
  public void deleteOutputTasksExcept(String outputToSpare, Task task) {
    for (String output : task.outputNames()) {
      if (!output.equals(outputToSpare)) {
        for (String taskID : task.taskIDsForOutput(output).get()) {
          recursivelyDeleteChildren(taskID);
        }
      }
    }
  }
}
