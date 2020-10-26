package org.testng.internal;

/**
 * Represents the capabilities of a simple container to hold data
 */
public interface IContainer<M> {

  /**
   * @return - Retrieves data from the container
   */
  M[] getItems();

  /**
   * @return - <code>true</code> if there are elements in the container.
   */
  boolean hasItems();

  /**
   * Clears the container
   */
  void clearItems();
}
