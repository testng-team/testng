package org.testng.internal;

import com.google.inject.Inject;

import org.testng.IConfigurable;
import org.testng.IHookable;
import org.testng.IObjectFactory;
import org.testng.internal.annotations.IAnnotationFinder;

public class Configuration implements IConfiguration {

  @Inject
  IAnnotationFinder m_annotationFinder;
  
  @Nullable
  @Inject
  IObjectFactory m_objectFactory;

  @Nullable
  @Inject
  IHookable m_hookable;

  @Nullable
  @Inject
  IConfigurable m_configurable;

  @Override
  public IAnnotationFinder getAnnotationFinder() {
    return m_annotationFinder;
  }

  @Override
  public IObjectFactory getObjectFactory() {
    return m_objectFactory;
  }

  @Override
  public IHookable getHookable() {
    return m_hookable;
  }

  @Override
  public void setHookable(IHookable h) {
    m_hookable = h;
  }

  @Override
  public IConfigurable getConfigurable() {
    return m_configurable;
  }

  @Override
  public void setConfigurable(IConfigurable c) {
    m_configurable = c;
  }
}