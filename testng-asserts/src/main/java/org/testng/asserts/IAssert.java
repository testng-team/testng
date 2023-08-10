package org.testng.asserts;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface IAssert<T> {
  @Nullable
  String getMessage();

  void doAssert();

  @Nullable
  T getActual();

  @Nullable
  T getExpected();
}
