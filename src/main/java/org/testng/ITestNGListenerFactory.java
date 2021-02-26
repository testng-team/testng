package org.testng;

/**
 * A factory used to create instances of ITestNGListener. Users can implement this interface in any
 * of their test classes but there can be only one such instance.
 */
public interface ITestNGListenerFactory {

  /**
   * Create and return an instance of the listener class passed in parameter. Return null if you
   * want to use the default factory.
   *
   * @param listenerClass The class of listener to create
   * @return The created listener
   */
  ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass);
}
