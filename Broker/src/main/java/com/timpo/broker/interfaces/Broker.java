package com.timpo.broker.interfaces;

import com.timpo.messaging.messaginginterfaces.ToBrokerMessages;
import com.timpo.messaging.messaginginterfaces.BrokerToCoordinator;
import com.timpo.messaging.messaginginterfaces.BrokerToResource;
import com.timpo.messaging.interfaces.MessageReceiver;
import com.timpo.messaging.interfaces.MessageSender;

public interface Broker {

  public MessageSender<BrokerToCoordinator> getBrokerToCoordinatorSender();

  public MessageSender<BrokerToResource> getBrokerToResourceSender();

  public MessageReceiver<ToBrokerMessages> getBrokerMessageReceiver();

  public MatchMaker getMatchMaker();

  public ResourceTracker getResourceTracker();

  public Auctioneer getAuctioneer();

  public Foreman getForeman();
}
