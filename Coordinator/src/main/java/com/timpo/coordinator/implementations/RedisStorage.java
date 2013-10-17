package com.timpo.coordinator.implementations;

import com.google.common.base.Optional;
import com.timpo.common.Utils;
import com.timpo.common.models.Job;
import com.timpo.common.models.Task;
import com.timpo.coordinator.interfaces.Storage;
import com.timpo.coordinator.interfaces.Translator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

public class RedisStorage extends Storage {

  private static final Logger LOG = Utils.logFor("RedisStorage");
  //
  private final JedisPool pool;
  private final Translator<Task> json;

  public RedisStorage(JedisPool pool, Translator<Task> json) {
    this.pool = pool;
    this.json = json;
  }

  @Override
  public void trackJob(Job job) {
    Jedis client = pool.getResource();
    try {
      Pipeline p = client.pipelined();

      String taskMapKey = getTaskMapKey();
      String jobMapKey = getJobMapKey(job);

      for (Task task : job.getTasks()) {
        String taskID = task.getID();
        String jsonTask = json.encode(task);

        //store the json-encdoded tasks in the task map
        p.hset(taskMapKey, taskID, jsonTask);

        //store the task_ids in the job map
        p.sadd(jobMapKey, taskID);
      }

      p.sync();
    } catch (Exception ex) {
      LOG.warn("problem with trackJob(" + job + "): {}", Utils.getLoggableException(ex));

    } finally {
      pool.returnResource(client);
    }
  }

  @Override
  public Optional<Job> getJob(String jobID) {
    Jedis client = pool.getResource();
    try {
      String jobMapKey = getJobMapKey(jobID);
      String taskMapKey = getTaskMapKey();

      //get the task id's from the job map
      Set<String> taskIDs = client.smembers(jobMapKey);

      Pipeline p = client.pipelined();

      //get all the tasks for those id's
      for (String taskID : taskIDs) {
        p.hget(taskMapKey, taskID);
      }

      List<Object> response = p.syncAndReturnAll();

      //parse the json tasks
      List<Task> tasks = new ArrayList<Task>();
      for (Object jsonTask : response) {
        tasks.add(json.decode((String) jsonTask));
      }

      return Optional.of(new Job(jobID, tasks));

    } catch (Exception ex) {
      LOG.warn("problem with getJob(" + jobID + "): {}", Utils.getLoggableException(ex));

      return Optional.absent();

    } finally {
      pool.returnResource(client);
    }
  }

  @Override
  public boolean deleteJob(String jobID) {
    Jedis client = pool.getResource();
    try {
      String jobMapKey = getJobMapKey(jobID);
      String taskMapKey = getTaskMapKey();

      //get the task id's from the job map
      Set<String> taskIDs = client.smembers(jobMapKey);

      Pipeline p = client.pipelined();

      //delete all the tasks associated with those id's
      for (String taskID : taskIDs) {
        p.hdel(taskMapKey, taskID);
      }

      //delete the job map
      p.del(jobMapKey);

      p.sync();

      //if the task id wasn't empty, then we successfully deleted the job
      return !taskIDs.isEmpty();

    } catch (Exception ex) {
      LOG.warn("problem with deleteJob(" + jobID + "): {}", Utils.getLoggableException(ex));

      return false;

    } finally {
      pool.returnResource(client);
    }
  }

  @Override
  public Optional<Task> retrieveTask(String taskID) {
    Jedis client = pool.getResource();
    try {
      String taskMapKey = getTaskMapKey();

      //get the task associated with the taskID
      String jsonTask = client.hget(taskMapKey, taskID);

      //decode the json-encoded task
      return Optional.of(json.decode(jsonTask));

    } catch (Exception ex) {
      LOG.warn("problem with retrieveTask(" + taskID + "): {}", Utils.getLoggableException(ex));

      return Optional.absent();

    } finally {
      pool.returnResource(client);
    }
  }

  @Override
  public void commitTask(Task task) {
    Jedis client = pool.getResource();
    try {
      String taskMapKey = getTaskMapKey();
      String taskID = task.getID();

      String jsonTask = json.encode(task);

      //json-encode this task and store it in the taskMap
      client.hset(taskMapKey, taskID, jsonTask);

    } catch (Exception ex) {
      LOG.warn("problem with commitTask(" + task + "): {}", Utils.getLoggableException(ex));

    } finally {
      pool.returnResource(client);
    }
  }

  @Override
  public boolean deleteTask(String taskID) {
    Jedis client = pool.getResource();
    try {
      String taskMapKey = getTaskMapKey();

      //delete this task from the taskMap
      client.hdel(taskMapKey, taskID);

      //TODO:delete the task from the job map

      return true;

    } catch (Exception ex) {
      LOG.warn("problem with deleteTask(" + taskID + "): {}", Utils.getLoggableException(ex));

      return false;

    } finally {
      pool.returnResource(client);
    }
  }

  public static String getTaskMapKey() {
    return "tasks";
  }

  private String getJobMapKey(Job job) {
    return getJobMapKey(job.getId());
  }

  private String getJobMapKey(String jobID) {
    return "job:" + jobID;
  }
}
