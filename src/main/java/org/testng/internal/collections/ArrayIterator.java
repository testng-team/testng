package org.testng.internal.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Custom iterator class over a 2D array */
public class ArrayIterator implements Iterator<Object[]> {

  private final Object[][] m_objects;
  private int m_count;

  public ArrayIterator(Object[][] objects) {
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
    return m_objects[m_count++];
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove operation is not supported on this iterator");
  }
}
