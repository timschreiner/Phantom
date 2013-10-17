package com.timpo.coordinator.interfaces;

import com.timpo.messaging.interfaces.MessageReceiver;
import com.timpo.messaging.interfaces.MessageSender;
import com.timpo.common.models.Job;
import com.timpo.messaging.messaginginterfaces.ToCoordinatorMessages;
import com.timpo.messaging.messaginginterfaces.CoordinatorToBroker;

public interface Coordinator {

  public abstract void shutdown();

  public Translator<Job> getTranslator();

  public Validator<Job> getValidator();

  public Storage getStorage();

  public TaskQueue getTaskQueue();

  public MessageReceiver<ToCoordinatorMessages> getCoordinatorReceiver();

  public MessageSender<CoordinatorToBroker> getCoordinatorToBrokerSender();
}
