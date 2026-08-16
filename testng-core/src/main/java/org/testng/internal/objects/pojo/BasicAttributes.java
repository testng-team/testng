package org.testng.internal.objects.pojo;

import org.jspecify.annotations.Nullable;
import org.testng.IClass;

/** Represents the basic attributes associated with object creation. */
public class BasicAttributes {

  private final @Nullable IClass iClass;
  private final @Nullable Class<?> clazz;

  public BasicAttributes(@Nullable IClass iClass, @Nullable Class<?> clazz) {
    this.iClass = iClass;
    this.clazz = clazz;
  }

  /** @return - The actual {@link Class} */
  public @Nullable Class<?> getRawClass() {
    return clazz;
  }

  /** @return - The wrapped {@link IClass} that represents a TestNG test class. */
  public @Nullable IClass getTestClass() {
    return iClass;
  }
}
