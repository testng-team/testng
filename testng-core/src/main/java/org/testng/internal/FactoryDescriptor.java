package org.testng.internal;

import org.testng.IFactory;

/**
 * The immutable {@link IFactory} handed to every instance a {@link FactoryMethod} produces.
 *
 * <p>A descriptor rather than the {@link FactoryMethod} itself: the latter is a mutable {@link
 * org.testng.ITestNGMethod} holding the test context and the data provider machinery, and every
 * produced instance keeps its factory for the whole run.
 */
public final class FactoryDescriptor implements IFactory {

  private final String name;
  private final Class<?> declaringClass;
  private final boolean lazy;

  FactoryDescriptor(ConstructorOrMethod com, boolean lazy) {
    this.name = com.getName();
    this.declaringClass = com.getDeclaringClass();
    this.lazy = lazy;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Class<?> getDeclaringClass() {
    return declaringClass;
  }

  @Override
  public boolean isLazy() {
    return lazy;
  }

  @Override
  public String toString() {
    return declaringClass.getName() + "." + name + (lazy ? " (lazy)" : "");
  }
}
