package org.testng.internal.annotations;

import org.testng.ITestNGListener;
import org.testng.annotations.IAnnotation;

public class ListenersAnnotation implements IListeners, IAnnotation {

  private Class<? extends ITestNGListener>[] value;

  @Override
  public Class<? extends ITestNGListener>[] getValue() {
    return value;
  }

  @Override
  public void setValue(Class<? extends ITestNGListener>[] value) {
    this.value = value;
  }

}
