package org.testng.internal.thread;

import java.io.Serializable;


/**
 * This class/interface
 */
public interface IAtomicInteger extends Serializable{
  /**
   * Get the current value.
   * @return the current value
   */
  int get();

  /**
   * Atomically increment by one the current value.
   *
   * @return the updated value
   */
  int incrementAndGet();
}
