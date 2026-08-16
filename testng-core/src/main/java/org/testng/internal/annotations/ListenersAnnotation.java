package org.testng.internal.annotations;

import org.testng.ITestNGListener;
import org.testng.annotations.IAnnotation;

public class ListenersAnnotation implements IListeners, IAnnotation {

  @SuppressWarnings("unchecked")
  private static final Class<? extends ITestNGListener>[] NO_LISTENERS =
      (Class<? extends ITestNGListener>[]) new Class<?>[0];

  // Listeners#value() defaults to {}; JDK15TagFactory sets the real value right after construction.
  private Class<? extends ITestNGListener>[] m_value = NO_LISTENERS;

  @Override
  public Class<? extends ITestNGListener>[] getValue() {
    return m_value;
  }

  @Override
  public void setValue(Class<? extends ITestNGListener>[] value) {
    m_value = value;
  }
}
