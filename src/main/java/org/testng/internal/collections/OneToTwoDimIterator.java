package org.testng.internal.collections;

import java.util.Iterator;

public class OneToTwoDimIterator implements Iterator<Object[]> {

  private final Iterator<Object> iterator;

  public OneToTwoDimIterator(Iterator<Object> iterator) {
    this.iterator = iterator;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public Object[] next() {
    return new Object[]{iterator.next()};
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove operation is not supported on this iterator");
  }
}
