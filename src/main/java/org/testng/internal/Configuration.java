package org.testng.internal;

import com.google.inject.Inject;
import com.google.inject.internal.Nullable;

import org.testng.IObjectFactory;
import org.testng.internal.annotations.IAnnotationFinder;

public class Configuration implements IConfiguration {

  @Inject
  IAnnotationFinder m_annotationFinder;
  
  @Inject
  @Nullable
  IObjectFactory m_objectFactory;

  public IAnnotationFinder getAnnotationFinder() {
    return m_annotationFinder;
  }

  public IObjectFactory getObjectFactory() {
    return m_objectFactory;
  }
}