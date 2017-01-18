package org.testng.internal.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom iterator class over a 2D array
 */
public class ArrayIterator implements Iterator<Object[]> {

  private final Object[][] objects;
  private int count;

  public ArrayIterator(Object[][] objects) {
    this.objects = objects;
    count = 0;
  }

  @Override
  public boolean hasNext() {
    return count < objects.length;
  }

  @Override
  public Object[] next() {
    if (count >= objects.length) {
      throw new NoSuchElementException();
    }
    return objects[count++];
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove operation is not supported on this iterator");
  }
}
