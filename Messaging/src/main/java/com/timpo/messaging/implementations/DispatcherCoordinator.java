package com.timpo.messaging.implementations;

import com.timpo.common.Utils;
import com.timpo.common.models.InvokeMethodMessage;
import com.timpo.messaging.interfaces.MessageDispatcher;
import com.timpo.messaging.messaginginterfaces.ToCoordinatorMessages;
import com.timpo.messaging.interfaces.MessageAcknowledger;
import java.util.Map;
import org.slf4j.Logger;

public class DispatcherCoordinator extends MessageDispatcher<ToCoordinatorMessages> {

  private static final Logger LOG = Utils.logFor("DispatcherCoordinator");

  public DispatcherCoordinator(ToCoordinatorMessages handler, MessageAcknowledger<ToCoordinatorMessages> ack) {
    super(handler, ack);
  }

  public void dispatch(String message) {
    InvokeMethodMessage imm;
    try {
      imm = Utils.fromJson(message, InvokeMethodMessage.class);
      Long expiration = imm.getExpiration();

      //TODO: is this the right behavior? should we always process expired messages
      if (expiration != null && Utils.currentTime() > expiration) {
        //ignore this message, it's expired
        LOG.debug("ephemeral message expired: {}", imm);

      } else {
        String method = imm.getMethod();
        ToCoordinatorMessages handler = getHandler();

        //match the method and params to the appropriate handler methods
        if (method.equals("acceptIncomingJobs")) {
          handler.acceptIncomingJobs(imm.paramBoolean("accept").get());

        } else if (method.equals("incomingJobRequest")) {
          handler.incomingJobRequest(imm.paramString("stringJob").get());

        } else if (method.equals("processingTask")) {
          handler.processingTask(imm.paramString("taskID").get(),
                  imm.paramString("resourceID").get());

        } else if (method.equals("resourcesAvailable")) {
          handler.resourcesAvailable(imm.paramString("taskID").get());

        } else if (method.equals("resourcesUnavailable")) {
          handler.resourcesUnavailable(imm.paramString("taskID").get());

        } else if (method.equals("taskFinished")) {
          handler.taskFinished(imm.paramString("taskID").get(),
                  imm.paramString("newState").get(),
                  (Map<String, Object>) imm.getParams().get("newParams"));

        } else {
          throw new IllegalArgumentException("method=" + method
                  + " does not match any of the methods in CoordinatorMessages");
        }

        //if a messageID was included, we need to acknowledge this message has
        //finished processing
        String messageID = imm.getId();
        if (messageID != null) {
          getMessageAcknowledger().acknowledgeMessage(messageID);
        }
      }

    } catch (Exception ex) {
      LOG.warn("unable to handle message: {} - {}", message, Utils.getLoggableException(ex));
    }
  }
}
