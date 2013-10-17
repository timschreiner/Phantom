package com.timpo.broker;

import com.timpo.broker.implementations.InMemoryAuctioneer;
import com.timpo.broker.implementations.InMemoryForeman;
import com.timpo.broker.implementations.InMemoryMatchMaker;
import com.timpo.broker.implementations.InMemoryResourceTracker;
import com.timpo.broker.interfaces.Auctioneer;
import com.timpo.broker.interfaces.Broker;
import com.timpo.broker.interfaces.Foreman;
import com.timpo.broker.interfaces.MatchMaker;
import com.timpo.broker.interfaces.ResourceTracker;
import com.timpo.broker.logic.BrokerMessageHandler;
import com.timpo.common.Constants;
import com.timpo.common.Utils;
import com.timpo.messaging.implementations.RedisReceiverBroker;
import com.timpo.messaging.implementations.RedisSenderBrokerToCoordinator;
import com.timpo.messaging.implementations.RedisSenderBrokerToResource;
import com.timpo.messaging.messaginginterfaces.ToBrokerMessages;
import com.timpo.messaging.messaginginterfaces.BrokerToCoordinator;
import com.timpo.messaging.messaginginterfaces.BrokerToResource;
import com.timpo.messaging.interfaces.MessageReceiver;
import com.timpo.messaging.interfaces.MessageSender;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import redis.clients.jedis.JedisPool;

public class DefaultBroker implements Broker {

  private final MessageSender<BrokerToCoordinator> brokerToCoordinatorSender;
  private final MessageSender<BrokerToResource> brokerToResourceSender;
  private final MessageReceiver<ToBrokerMessages> brokerMessageReceiver;
  private final MatchMaker matchMaker;
  private final ResourceTracker resourceTracker;
  private final Auctioneer auctioneer;
  private final Foreman foreman;

  public DefaultBroker() {
    String redisHostname = "localhost";
    int redisPort = 6379;
    JedisPool redisPool = new JedisPool(redisHostname, redisPort);

    String brokerID = Constants.Defaults.BROKER_ID;
    String coordinatorID = Constants.Defaults.COORDINATOR_ID;


    brokerToCoordinatorSender = new RedisSenderBrokerToCoordinator(redisPool,
            brokerID, coordinatorID);


    brokerToResourceSender = new RedisSenderBrokerToResource(redisPool, brokerID);


    matchMaker = new InMemoryMatchMaker(new ConcurrentHashMap<String, Set<String>>());


    resourceTracker = new InMemoryResourceTracker(Utils.makeConcurrentSet());


    auctioneer = new InMemoryAuctioneer(new ConcurrentHashMap<String, PriorityQueue<Auctioneer.Bid>>(), brokerToResourceSender);


    foreman = new InMemoryForeman(brokerToResourceSender);


    ToBrokerMessages brokerMessageHandler = new BrokerMessageHandler(matchMaker,
            resourceTracker, auctioneer, foreman, brokerToCoordinatorSender,
            brokerToResourceSender);
    brokerMessageReceiver = new RedisReceiverBroker(redisPool, brokerID,
            brokerMessageHandler);


    //listen for incoming requests
    brokerMessageReceiver.start();
  }

  public MatchMaker getMatchMaker() {
    return matchMaker;
  }

  public ResourceTracker getResourceTracker() {
    return resourceTracker;
  }

  public Auctioneer getAuctioneer() {
    return auctioneer;
  }

  public Foreman getForeman() {
    return foreman;
  }

  public MessageSender<BrokerToCoordinator> getBrokerToCoordinatorSender() {
    return brokerToCoordinatorSender;
  }

  public MessageSender<BrokerToResource> getBrokerToResourceSender() {
    return brokerToResourceSender;
  }

  public MessageReceiver<ToBrokerMessages> getBrokerMessageReceiver() {
    return brokerMessageReceiver;
  }
}
