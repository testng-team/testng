package org.testng.asserts;

public interface IAssert<T> {
  String getMessage();
  void doAssert();
  T getActual();
  T getExpected();
}
