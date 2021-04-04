package org.testng.internal.objects;

import org.testng.IInjectorFactory;

public interface ISuiteContext {

  String getParentModule();

  String getGuiceStage();

  String getName();

  IInjectorFactory getInjectorFactory();
}
