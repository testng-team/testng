package org.testng;

import org.jspecify.annotations.Nullable;

/** This interface captures a test method along with all the instances it should be run on. */
public interface IMethodInstance {

  ITestNGMethod getMethod();

  /**
   * @return The instance the method will be invoked on, or {@code null} when the method carries no
   *     instance.
   */
  @Nullable
  Object getInstance();
}
