package com.timpo.broker.implementations;

import com.timpo.broker.interfaces.Foreman;
import com.timpo.messaging.interfaces.MessageSender;
import com.timpo.common.models.Task;
import com.timpo.messaging.messaginginterfaces.BrokerToResource;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisBackedForeman implements Foreman {

  private final JedisPool pool;
  private final MessageSender<BrokerToResource> brokerToResourceSender;

  public RedisBackedForeman(JedisPool pool, MessageSender<BrokerToResource> brokerToResourceSender) {
    this.pool = pool;
    this.brokerToResourceSender = brokerToResourceSender;
  }

  public boolean claimTask(String taskID, String resourceID) {
    Jedis client = pool.getResource();
    try {
      String key = getClaimKey(taskID);

      //try to set claim this task for yourself. 1 means you were the first to
      //try to claim it and succeeded, anything else means you failed
      return client.setnx(key, resourceID) == 1;

    } finally {
      pool.returnResource(client);
    }
  }

  public void offerTask(Task task, String resourceID) {
    brokerToResourceSender.send().offer(task, resourceID);
  }

  public void assignTask(String taskID, String resourceID) {
    brokerToResourceSender.send().processTask(taskID, resourceID);
  }

  public void createClaimsTracker(String taskID) {
    //do nothing, redis will create it for us
  }

  public void cleanupClaimsTracker(String taskID) {
    Jedis client = pool.getResource();
    try {
      String key = getClaimKey(taskID);

      client.del(key);

    } finally {
      pool.returnResource(client);
    }
  }

  private String getClaimKey(String taskID) {
    return "claim:" + taskID;
  }

  public boolean taskClaimed(String taskID) {
    Jedis client = pool.getResource();
    try {
      String key = getClaimKey(taskID);

      return client.get(key).equals("nil");

    } finally {
      pool.returnResource(client);
    }
  }
}
