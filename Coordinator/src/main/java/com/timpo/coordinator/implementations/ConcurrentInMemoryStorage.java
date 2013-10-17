package com.timpo.coordinator.implementations;

import com.google.common.base.Optional;
import com.timpo.common.models.Job;
import com.timpo.common.models.Task;
import com.timpo.coordinator.interfaces.Storage;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentInMemoryStorage extends Storage {

  private final ConcurrentMap<String, Task> taskMap;
  private final ConcurrentMap<String, Job> jobMap;
  private final ConcurrentMap<Task, Job> taskToJobMap;

  public ConcurrentInMemoryStorage(ConcurrentMap<String, Task> taskMap, ConcurrentMap<String, Job> jobMap, ConcurrentMap<Task, Job> taskToJobMap) {
    this.taskMap = taskMap;
    this.jobMap = jobMap;
    this.taskToJobMap = taskToJobMap;
  }

  @Override
  public void trackJob(Job job) {
    //track the job
    jobMap.put(job.getId(), job);

    for (Task task : job.getTasks()) {
      //track the individual tasks
      taskMap.put(task.getID(), task);

      //track the job associated with each task, to make cleanup easier
      taskToJobMap.put(task, job);
    }
  }

  @Override
  public Optional<Job> getJob(String jobID) {
    Job job = jobMap.get(jobID);
    if (job == null) {
      return Optional.absent();
    } else {
      return Optional.of(job);
    }
  }

  @Override
  public boolean deleteJob(String jobID) {
    Job job = jobMap.remove(jobID);
    if (job != null) {
      //delete all references to this task
      for (Task task : job.getTasks()) {
        taskMap.remove(task);

        taskToJobMap.remove(task);
      }

      return true;

    } else {
      return false;
    }
  }

  @Override
  public Optional<Task> retrieveTask(String taskID) {
    Task task = taskMap.get(taskID);
    if (task == null) {
      return Optional.absent();
    } else {
      return Optional.of(task);
    }
  }

  @Override
  public boolean deleteTask(String taskID) {
    Task task = taskMap.remove(taskID);
    if (task != null) {
      Job job = taskToJobMap.remove(task);

      List<Task> tasks = job.getTasks();
      //TODO: is this the best way to do this?
      synchronized (tasks) {
        tasks.remove(task);
      }

      return true;
    } else {
      return false;
    }
  }

  @Override
  public void commitTask(Task task) {
    //since we are already tracking the task directly, no special action needs
    //to take place to store changes to the task
  }
}
