package org.testng.internal.collections;

import java.util.Iterator;

public class OneToTwoDimIterator implements Iterator<Object[]> {

  private final Iterator<Object> m_iterator;

  public OneToTwoDimIterator(Iterator<Object> iterator) {
    m_iterator = iterator;
  }

  @Override
  public boolean hasNext() {
    return m_iterator.hasNext();
  }

  @Override
  public Object[] next() {
    return new Object[] {m_iterator.next()};
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove operation is not supported on this iterator");
  }
}
