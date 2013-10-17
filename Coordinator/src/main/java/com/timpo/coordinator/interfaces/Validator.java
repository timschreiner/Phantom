package com.timpo.coordinator.interfaces;

import javax.validation.ValidationException;

/**
 * Used to determine that a Job is processable
 */
public interface Validator<T> {

  /**
   * Determines whether a T is valid
   *
   * @param t to validate
   */
  public void validate(T t) throws ValidationException;
}
