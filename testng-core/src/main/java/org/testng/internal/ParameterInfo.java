package org.testng.internal;

import org.testng.IFactoryInstance;

public class ParameterInfo implements IParameterInfo {
  private final Object instance;
  private final FactoryInstance factoryInstance;

  public ParameterInfo(Object instance, FactoryInstance factoryInstance) {
    this.instance = instance;
    this.factoryInstance = factoryInstance;
  }

  @Override
  public Object getInstance() {
    return instance;
  }

  @Override
  @Deprecated
  public int getIndex() {
    return factoryInstance.getInvocationIndex();
  }

  @Override
  public Object[] getParameters() {
    return factoryInstance.rawParameters();
  }

  @Override
  public IFactoryInstance getFactoryInstance() {
    return factoryInstance;
  }
}
