package org.testng.annotations;

import org.testng.ITestNGListener;

public interface IListenersAnnotation extends IAnnotation {

  Class<? extends ITestNGListener>[] getValue();

  void setValue(Class<? extends ITestNGListener>[] value);
}
