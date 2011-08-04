package org.testng.internal.annotations;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Sets {

  public static <T> Set<T> newHashSet() {
    return new HashSet<T>();
  }

  public static <T> Set<T> newLinkedHashSet() {
    return new LinkedHashSet<T>();
  }

}
