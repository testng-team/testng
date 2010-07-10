package org.testng.internal;

import org.testng.IObjectFactory;
import org.testng.internal.annotations.IAnnotationFinder;

public interface IConfiguration {
  IAnnotationFinder getAnnotationFinder();

  IObjectFactory getObjectFactory();
}
