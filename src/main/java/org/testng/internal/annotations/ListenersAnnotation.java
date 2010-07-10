package org.testng.internal.annotations;

import org.testng.ITestNGListener;
import org.testng.annotations.IAnnotation;

public class ListenersAnnotation implements IListeners, IAnnotation {

  private Class<? extends ITestNGListener>[] m_value;

  public Class<? extends ITestNGListener>[] getValue() {
    return m_value;
  }

  public void setValue(Class<? extends ITestNGListener>[] value) {
    m_value = value;
  }

}
