package org.testng.internal;

import org.testng.IConfigurable;
import org.testng.IHookable;
import org.testng.IObjectFactory;
import org.testng.internal.annotations.IAnnotationFinder;

public interface IConfiguration {
  IAnnotationFinder getAnnotationFinder();

  IObjectFactory getObjectFactory();

  IHookable getHookable();

  IConfigurable getConfigurable();
}
