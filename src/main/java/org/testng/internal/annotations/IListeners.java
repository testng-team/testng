package org.testng.internal.annotations;

import org.testng.ITestNGListener;

public interface IListeners {
  Class<? extends ITestNGListener>[] getValue();

  void setValue(Class<? extends ITestNGListener>[] value);
}
