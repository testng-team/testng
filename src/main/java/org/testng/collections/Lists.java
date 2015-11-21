package org.testng.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class Lists {

  private Lists() {}

  public static <K> List<K> newArrayList() {
    return new ArrayList<>();
  }

  public static <K> List<K> newArrayList(Collection<K> c) {
    return new ArrayList<>(c);
  }

  public static <K> List<K> newArrayList(K... elements) {
    List<K> result = new ArrayList<>();
    Collections.addAll(result, elements);
    return result;
  }

  public static <K> List<K> newArrayList(int size) {
    return new ArrayList<>(size);
  }
  
  /**
   * convert iterator to List
   * 
   * @param iterator
   * @return
   */
  public static <K> List<K> newArrayList(Iterator<K> iterator) {
      List<K> copy = new ArrayList<K>();
      while (iterator.hasNext())
          copy.add(iterator.next());
      return copy;
  }
}
