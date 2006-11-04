package org.testng.internal.thread;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class/interface 
 */
public class AtomicIntegerAdapter implements IAtomicInteger {
  private AtomicInteger m_atomicInteger;
  
  public AtomicIntegerAdapter(int initialValue) {
    m_atomicInteger= new AtomicInteger(initialValue);
  }
  
  /**
   * @see org.testng.internal.thread.IAtomicInteger#get()
   */
  public int get() {
    return m_atomicInteger.get();
  }

  /**
   * @see org.testng.internal.thread.IAtomicInteger#incrementAndGet()
   */
  public int incrementAndGet() {
    return m_atomicInteger.incrementAndGet();
  }

}
