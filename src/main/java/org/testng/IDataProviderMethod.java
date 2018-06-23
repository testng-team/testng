package org.testng;

import java.lang.reflect.Method;
import java.util.List;

/** Represents the attributes of a {@link org.testng.annotations.DataProvider} annotated method. */
public interface IDataProviderMethod {
  /**
   * @return - The instance to which the data provider belongs to. <code>null</code> if the data
   *     provider is a static one.
   */
  Object getInstance();

  /**
   * @return - A {@link Method} object that represents the actual {@literal @}{@link
   *     org.testng.annotations.DataProvider} method.
   */
  Method getMethod();

  /** The name of this DataProvider. */
  String getName();

  /** Whether this data provider should be run in parallel. */
  boolean isParallel();

  /** Which indices to run from this data provider, default: all. */
  List<Integer> getIndices();
}
