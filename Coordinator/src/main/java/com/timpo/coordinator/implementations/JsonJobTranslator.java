package com.timpo.coordinator.implementations;

import com.timpo.common.Utils;
import com.timpo.common.models.Job;
import com.timpo.coordinator.interfaces.Translator;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

public class JsonJobTranslator implements Translator<Job> {

  public Job decode(String stringRepresentation) throws DecoderException {
    try {
      return Utils.fromJson(stringRepresentation, Job.class);
    } catch (Exception ex) {
      throw new DecoderException(ex);
    }
  }

  public String encode(Job object) throws EncoderException {
    try {
      return Utils.toJson(object);
    } catch (Exception ex) {
      throw new EncoderException(ex);
    }
  }
}
