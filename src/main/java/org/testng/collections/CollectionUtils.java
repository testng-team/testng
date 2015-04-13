package org.testng.collections;

import java.util.Collection;
import java.util.Map;

public final class CollectionUtils {

  private CollectionUtils() {}

  public static boolean hasElements(Collection<?> c) {
    return c != null && ! c.isEmpty();
  }

  public static boolean hasElements(Map<?, ?> c) {
    return c != null && ! c.isEmpty();
  }

}
