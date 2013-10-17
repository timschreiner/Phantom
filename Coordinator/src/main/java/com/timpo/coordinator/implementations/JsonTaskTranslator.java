package com.timpo.coordinator.implementations;

import com.timpo.common.Utils;
import com.timpo.common.models.Task;
import com.timpo.coordinator.interfaces.Translator;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

public class JsonTaskTranslator implements Translator<Task> {

  public Task decode(String taskJSON) throws DecoderException {
    try {
      return Utils.fromJson(taskJSON, Task.class);
    } catch (Exception ex) {
      throw new DecoderException(ex);
    }
  }

  public String encode(Task task) throws EncoderException {
    try {
      return Utils.toJson(task);
    } catch (Exception ex) {
      throw new EncoderException(ex);
    }
  }
}
