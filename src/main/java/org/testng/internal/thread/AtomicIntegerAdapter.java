package org.testng.internal.thread;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class/interface
 */
public class AtomicIntegerAdapter implements IAtomicInteger {
  /**
   *
   */
  private static final long serialVersionUID = -6295904797532558594L;
  private AtomicInteger m_atomicInteger;

  public AtomicIntegerAdapter(int initialValue) {
    m_atomicInteger= new AtomicInteger(initialValue);
  }

  /**
   * @see org.testng.internal.thread.IAtomicInteger#get()
   */
  @Override
  public int get() {
    return m_atomicInteger.get();
  }

  /**
   * @see org.testng.internal.thread.IAtomicInteger#incrementAndGet()
   */
  @Override
  public int incrementAndGet() {
    return m_atomicInteger.incrementAndGet();
  }

}
