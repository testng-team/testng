package org.testng.internal;

import org.testng.IAnnotationTransformer;
import org.testng.IConfigurable;
import org.testng.IHookable;
import org.testng.ITestObjectFactory;
import org.testng.internal.annotations.IAnnotationFinder;

public interface IConfiguration {
  IAnnotationFinder getAnnotationFinder();

  ITestObjectFactory getObjectFactory();
  void setObjectFactory(ITestObjectFactory m_objectFactory);

  IHookable getHookable();
  void setHookable(IHookable h);

  IConfigurable getConfigurable();
  void setConfigurable(IConfigurable c);

}
