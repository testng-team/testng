package org.testng.internal;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.testng.IInjectorFactory;

class GuiceBackedInjectorFactory implements IInjectorFactory {

  @Override
  public Injector getInjector(Stage stage, Module... modules) {
    return Guice.createInjector(stage, modules);
  }
}
