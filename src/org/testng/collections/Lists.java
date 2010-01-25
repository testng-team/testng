package org.testng.collections;

import java.util.ArrayList;
import java.util.List;

public class Lists {

  public static <K> List<K> newArrayList() {
    return new ArrayList<K>();
  }

  public static <K> List<K> newArrayList(int size) {
    return new ArrayList<K>(size);
  }
}
