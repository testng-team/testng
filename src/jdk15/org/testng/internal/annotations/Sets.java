package org.testng.internal.annotations;

import java.util.HashSet;
import java.util.Set;

public class Sets {

  public static <K> Set<K> newHashSet() {
    return new HashSet<K>();
  }

}
