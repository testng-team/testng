package org.testng.internal;

import java.util.List;
import org.testng.*;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.thread.IExecutorFactory;

public interface IConfiguration {

  IAnnotationFinder getAnnotationFinder();

  void setAnnotationFinder(IAnnotationFinder finder);

  ITestObjectFactory getObjectFactory();

  void setObjectFactory(ITestObjectFactory m_objectFactory);

  IHookable getHookable();

  void setHookable(IHookable h);

  IConfigurable getConfigurable();

  void setConfigurable(IConfigurable c);

  List<IExecutionListener> getExecutionListeners();

  default void addExecutionListener(IExecutionListener l) {
  }

  default boolean addExecutionListenerIfAbsent(IExecutionListener l) {
    return false;
  }

  List<IConfigurationListener> getConfigurationListeners();

  void addConfigurationListener(IConfigurationListener cl);

  boolean alwaysRunListeners();

  void setAlwaysRunListeners(boolean alwaysRun);

  IExecutorFactory getExecutorFactory();

  void setExecutorFactory(IExecutorFactory factory);

  IInjectorFactory getInjectorFactory();

  void setInjectorFactory(IInjectorFactory factory);

  boolean getOverrideIncludedMethods();

  void setOverrideIncludedMethods(boolean overrideIncludedMethods);
}
