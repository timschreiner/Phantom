package com.timpo.messaging.implementations.redis;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class RedisMessageHelpers {

  public static String makeIncomingMessageKey(String receiverID) {
    return "msgs:" + receiverID;
  }

  public static String makeMessageAcknowledgmentKey(String receiverID) {
    return "bkup:" + receiverID;
  }

  public static Builder<String, Object> newObjectMap() {
    return new ImmutableMap.Builder<String, Object>();
  }
}
