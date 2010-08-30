package org.testng.internal;

import com.google.inject.Inject;

import org.testng.IObjectFactory;
import org.testng.internal.annotations.IAnnotationFinder;

public class Configuration implements IConfiguration {

  @Inject
  IAnnotationFinder m_annotationFinder;
  
  @Nullable
  @Inject
  IObjectFactory m_objectFactory;

  @Override
  public IAnnotationFinder getAnnotationFinder() {
    return m_annotationFinder;
  }

  @Override
  public IObjectFactory getObjectFactory() {
    return m_objectFactory;
  }
}