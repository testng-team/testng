package org.testng.internal.annotations;

import org.testng.ITestNGListener;
import org.testng.annotations.IAnnotation;

public interface IListeners extends IAnnotation {
  Class<? extends ITestNGListener>[] getValue();

  void setValue(Class<? extends ITestNGListener>[] value);
}
