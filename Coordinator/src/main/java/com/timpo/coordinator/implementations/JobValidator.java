package com.timpo.coordinator.implementations;

import com.timpo.common.models.Job;
import com.timpo.common.models.Task;
import com.timpo.coordinator.interfaces.Validator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.ValidationException;

public class JobValidator implements Validator<Job> {

  //TODO: should we ensure that none of these task id's are already being tracked?
  /**
   * Determines whether a Job is valid (ie., no task fields are null, contains
   * no cycles, the first element is the root element, etc.)
   *
   * @param job to validate
   *
   * @return true if the Job is valid
   */
  public void validate(Job job) throws ValidationException {
    Set<String> outputTaskIDs = new HashSet<String>();
    Set<String> taskIDs = new HashSet<String>();

    List<Task> tasks = job.getTasks();

    if (tasks == null || tasks.isEmpty()) {
      throw new ValidationException("every job must contain at least one task");
    }

    //since we are putting the first task into the set, if any other task
    //references it, we'll know it's not the root task
    String rootTaskID = tasks.get(0).getID();
    outputTaskIDs.add(rootTaskID);

    for (Task task : tasks) {
      //each task should contain no null fields
      if (task.getID() == null
              || task.currentState() == null
              || task.getRequirements() == null
              || task.getParams() == null
              || task.outputNames() == null) {
        throw new ValidationException("no fields on task can be null");
      }

      //track the id's so we can make sure that all specified outputs exist in
      //the job tree
      taskIDs.add(task.getID());

      //outputs should be unique and contain no cycles
      for (String output : task.outputNames()) {
        //there's no need to check if the output is valid since i'm iterating
        //through the outputs
        for (String taskID : task.taskIDsForOutput(output).get()) {
          if (taskID.equals(rootTaskID)) {
            //the root id isn't really the root id if some other task points to it
            throw new ValidationException("the first job task is not the root element");

          } else if (outputTaskIDs.add(taskID) == false) {
            //some other task already referenced this taskID in their outputs
            throw new ValidationException("taskID=" + taskID
                    + " referenced more than once in outputs");
          }
        }
      }
    }

    //ensure all outputs point to valid tasks
    for (String taskID : outputTaskIDs) {
      if (!taskIDs.contains(taskID)) {
        throw new ValidationException("output taskID=" + taskID
                + " is not a member of this job");
      }
    }

    //if none of the above tests threw an exception, this task looks good
  }
}
