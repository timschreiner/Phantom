package com.timpo.common.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.timpo.common.Utils;
import java.util.Map;
import org.slf4j.Logger;

public class InvokeMethodMessage {

  private static final Logger LOG = Utils.logFor("InvokeMethodMessage");
  //
  private static final Long NO_EXPIRATION = null;
  private static final String NO_MESSAGE_ID = null;
  private final String method;
  private final String from;
  private final Map<String, Object> params;
  private final Long expiration;
  private final String id;

  @JsonCreator
  public InvokeMethodMessage(
          @JsonProperty("method") String method,
          @JsonProperty("from") String from,
          @JsonProperty("params") Map<String, Object> params,
          @JsonProperty("expiration") Long expiration,
          @JsonProperty("id") String id) {
    this.method = method;
    this.from = from;
    this.params = params;
    this.expiration = expiration;
    this.id = id;
  }

  public static InvokeMethodMessage durable(String method, String from, Map<String, Object> params, String messageID) {
    return new InvokeMethodMessage(method, from, params, NO_EXPIRATION, messageID);
  }

  public static InvokeMethodMessage ephemeral(String method, String from, Map<String, Object> params, Long expiration) {
    return new InvokeMethodMessage(method, from, params, expiration, NO_MESSAGE_ID);
  }

  public String toJson() throws Exception {
    return Utils.toJson(this);
  }

  public Optional<Task> paramTask() {
    String name = "task";
    try {
      return Optional.of(new Task((Map<String, Object>) param(name)));

    } catch (Exception ex) {
      LOG.warn("unable to find task param: " + name);
      return Optional.absent();
    }
  }

  public Optional<Map<String, String>> paramRequirements() {
    String name = "requirements";
    try {
      return Optional.of((Map<String, String>) param(name));

    } catch (Exception ex) {
      LOG.warn("unable to find requirements param: " + name);
      return Optional.absent();
    }
  }

  public Optional<String> paramString(String name) {
    try {
      return Optional.of((String) param(name));

    } catch (Exception ex) {
      LOG.warn("unable to find String param: " + name);
      return Optional.absent();
    }
  }

  public Optional<Double> paramDouble(String name) {
    try {
      return Optional.of((Double) param(name));

    } catch (Exception ex) {
      LOG.warn("unable to find Double param: " + name);
      return Optional.absent();
    }
  }

  public Optional<Boolean> paramBoolean(String name) {
    try {
      return Optional.of((Boolean) param(name));

    } catch (Exception ex) {
      LOG.warn("unable to find Boolean param: " + name);
      return Optional.absent();
    }
  }

  private Object param(String name) {
    return this.getParams().get(name);
  }

  //<editor-fold defaultstate="collapsed" desc="generated">
  public String getMethod() {
    return method;
  }

  public String getFrom() {
    return from;
  }

  public String getId() {
    return id;
  }

  public Long getExpiration() {
    return expiration;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  @Override
  public String toString() {
    return "Message{" + "method=" + method + ", from=" + from + ", params=" + params + ", expiration=" + expiration + ", messageID=" + id + '}';
  }
  //</editor-fold>
}
