package org.testng.internal.dynamicgraph;

import org.testng.ITestNGMethod;
import org.testng.internal.WrappedTestNGMethod;

public class FakeWrappedFactoryMethod extends WrappedTestNGMethod {

  private Object instance;

  public FakeWrappedFactoryMethod(ITestNGMethod testNGMethod, Object instance) {
    super(testNGMethod);
    this.instance = instance;
  }

  @Override
  public Object getInstance() {
    return instance;
  }
}
