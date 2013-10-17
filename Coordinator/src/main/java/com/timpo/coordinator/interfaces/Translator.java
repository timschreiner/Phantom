package com.timpo.coordinator.interfaces;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

/**
 * Turn a string representation of an object (json, xml, etc.) into an instance
 * of that object
 *
 * @param <T>
 */
public interface Translator<T> {

  /**
   * Turn a string representation of T back into an instance of T
   *
   * @param stringRepresentation
   * @return T instance
   * @throws DecoderException
   */
  public T decode(String stringRepresentation) throws DecoderException;

  /**
   * Turn an instance of T into a string representation
   *
   * @param T instance
   * @return stringRepresentation of T
   * @throws DecoderException
   */
  public String encode(T object) throws EncoderException;
}
