package org.testng.internal.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;

/** Custom iterator class over a 2D array */
public class ArrayIterator implements Iterator<Object @Nullable []> {

  private final Object[] @Nullable [] m_objects;
  private int m_count;

  public ArrayIterator(Object[] @Nullable [] objects) {
    m_objects = objects;
    m_count = 0;
  }

  @Override
  public boolean hasNext() {
    return m_count < m_objects.length;
  }

  @Override
  public Object @Nullable [] next() {
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
