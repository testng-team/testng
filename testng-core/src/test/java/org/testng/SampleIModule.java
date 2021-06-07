package org.testng;

import com.google.inject.Module;

public final class SampleIModule implements IModule {
  private static final Module module = binder -> {};

  @Override
  public Module getModule() {
    return module;
  }
}
