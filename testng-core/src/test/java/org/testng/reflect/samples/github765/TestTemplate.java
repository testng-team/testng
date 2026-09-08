package org.testng.reflect.samples.github765;

public abstract class TestTemplate<T> {

  public abstract void callExecuteTest(T testParameters) throws Exception;
}
