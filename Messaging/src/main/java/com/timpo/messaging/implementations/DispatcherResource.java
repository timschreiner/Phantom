package com.timpo.messaging.implementations;

import com.timpo.common.Utils;
import com.timpo.common.models.InvokeMethodMessage;
import com.timpo.messaging.interfaces.MessageDispatcher;
import com.timpo.messaging.interfaces.MessageAcknowledger;
import com.timpo.messaging.messaginginterfaces.ToResourceMessages;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;

public class DispatcherResource extends MessageDispatcher<ToResourceMessages> {

  private static final Logger LOG = Utils.logFor("DispatcherResource");
  //
  //NOTE: because the resourceIDs are only used to route the message to a
  //resource and have no use in handling it, they are not included in the
  //message object and cannot be parsed
  private static final String EMPTY_RESOURCE_ID = "";
  private static final Set<String> EMPTY_RESOURCE_IDS = new HashSet<String>();

  public DispatcherResource(ToResourceMessages handler, MessageAcknowledger<ToResourceMessages> ack) {
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
        ToResourceMessages handler = getHandler();


        //match the method and params to the appropriate handler methods
        if (method.equals("cancelOffer")) {
          handler.cancelOffer(imm.paramString("taskID").get(),
                  EMPTY_RESOURCE_ID);

        } else if (method.equals("offer")) {
          handler.offer(imm.paramTask().get(),
                  EMPTY_RESOURCE_ID);

        } else if (method.equals("processTask")) {
          handler.processTask(imm.paramString("taskID").get(),
                  EMPTY_RESOURCE_ID);

        } else if (method.equals("requestBids")) {
          handler.requestBids(imm.paramRequirements().get(),
                  imm.paramString("taskID").get(),
                  EMPTY_RESOURCE_IDS);

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
