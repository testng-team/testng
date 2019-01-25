package org.testng.internal;

/**
 * Represents the ability to retrieve the parameters associated with a factory method.
 */
public interface IParameterInfo {

  /**
   * @return - The actual instance associated with a factory method
   */
  Object getInstance();

  /**
   * @return - The parameters associated with the factory method as an array.
   */
  Object[] getParameters();

  static Object embeddedInstance(Object original) {
    if (original instanceof IParameterInfo) {
      return ((IParameterInfo) original).getInstance();
    }
    return original;
  }

}
