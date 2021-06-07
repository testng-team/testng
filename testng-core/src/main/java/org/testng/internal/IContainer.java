package org.testng.internal;

/** Represents the capabilities of a simple container to hold data */
public interface IContainer<M> {

  /** @return - Retrieves data from the container */
  M[] getItems();

  /** Clears the container */
  void clearItems();

  /** @return - <code>true</code> if the container items were cleared. */
  boolean isCleared();
}
