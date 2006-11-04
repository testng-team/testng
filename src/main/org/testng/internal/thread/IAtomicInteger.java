package org.testng.internal.thread;


/**
 * This class/interface 
 */
public interface IAtomicInteger {
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
