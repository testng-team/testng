package org.testng.internal.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class OneToTwoDimArrayIterator implements Iterator<Object[]> {

  private final Object[] m_objects;
  private int m_count;

  public OneToTwoDimArrayIterator(Object[] objects) {
    m_objects = objects;
    m_count = 0;
  }

  @Override
  public boolean hasNext() {
    return m_count < m_objects.length;
  }

  @Override
  public Object[] next() {
    if (m_count >= m_objects.length) {
      throw new NoSuchElementException();
    }
    return new Object[] {m_objects[m_count++]};
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove operation is not supported on this iterator");
  }
}
