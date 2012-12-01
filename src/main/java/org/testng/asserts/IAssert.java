package org.testng.asserts;

public interface IAssert {
  String getMessage();
  void doAssert();
  Object getActual();
  Object getExpected();
}
