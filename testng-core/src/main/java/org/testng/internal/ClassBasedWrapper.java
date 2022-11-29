package org.testng.internal;

import java.util.Objects;

public final class ClassBasedWrapper<T> {

  private final T object;

  private ClassBasedWrapper(T object) {
    this.object = object;
  }

  public static <T> ClassBasedWrapper<T> wrap(T object) {
    return new ClassBasedWrapper<>(object);
  }

  public T unWrap() {
    return object;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ClassBasedWrapper<?> wrapper = (ClassBasedWrapper<?>) o;
    return object.getClass().equals(wrapper.object.getClass());
  }

  @Override
  public int hashCode() {
    return Objects.hash(object);
  }
}
