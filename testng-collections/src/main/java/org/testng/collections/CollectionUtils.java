package org.testng.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class CollectionUtils {

  private CollectionUtils() {}

  public static boolean hasElements(@Nullable Collection<?> c) {
    return c != null && !c.isEmpty();
  }

  public static boolean hasElements(@Nullable Map<?, ?> c) {
    return c != null && !c.isEmpty();
  }

  public static <T> Iterable<T> asIterable(Iterator<T> iterator) {
    return () -> iterator;
  }
}
