package org.testng.internal;

import com.beust.jbus.IBus;

import org.testng.IConfigurable;
import org.testng.IHookable;
import org.testng.IObjectFactory;
import org.testng.internal.annotations.IAnnotationFinder;

public interface IConfiguration {
  IAnnotationFinder getAnnotationFinder();

  IObjectFactory getObjectFactory();

  IHookable getHookable();
  void setHookable(IHookable h);

  IConfigurable getConfigurable();
  void setConfigurable(IConfigurable c);

  IBus getBus();
}
