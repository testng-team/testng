package org.testng.internal;

import java.util.Objects;

/**
 * Wraps an object, so that two wrappers are equal when their objects have the same class.
 *
 * <p>Note: {@link #hashCode()} does not match {@link #equals(Object)}. It uses the hash code of the
 * object, not of its class. So two equal wrappers can have different hash codes. A hash-based
 * collection, or {@code Stream.distinct()}, can then keep both of them.
 *
 * @param <T> the type of the wrapped object.
 */
public final class ClassBasedWrapper<T> {

  private final T object;

  private ClassBasedWrapper(T object) {
    this.object = object;
  }

  /**
   * Wraps {@code object}.
   *
   * @param object the object to wrap.
   * @param <T> the type of the object.
   * @return the new wrapper.
   */
  public static <T> ClassBasedWrapper<T> wrap(T object) {
    return new ClassBasedWrapper<>(object);
  }

  /**
   * Returns the wrapped object.
   *
   * @return the object that {@link #wrap(Object)} received.
   */
  public T unWrap() {
    return object;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClassBasedWrapper<?> wrapper = (ClassBasedWrapper<?>) o;
    return object.getClass().equals(wrapper.object.getClass());
  }

  @Override
  public int hashCode() {
    return Objects.hash(object);
  }
}
