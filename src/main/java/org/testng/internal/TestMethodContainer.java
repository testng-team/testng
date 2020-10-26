package org.testng.internal;

import java.util.Arrays;
import org.testng.ITestNGMethod;

public final class TestMethodContainer implements IContainer<ITestNGMethod> {

  private ITestNGMethod[] methods;

  public TestMethodContainer(ITestNGMethod[] methods) {
    this.methods = methods;
  }

  public ITestNGMethod[] getItems() {
    return methods;
  }

  public boolean hasItems() {
    return methods != null && methods.length != 0;
  }

  public void clearItems() {
    if (methods == null) {
      return;
    }
    Arrays.fill(methods, null);
    methods = null;
  }
}
