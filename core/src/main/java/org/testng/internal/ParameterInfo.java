package org.testng.internal;

public class ParameterInfo implements IParameterInfo {
  private Object instance;
  private Object[] parameters;

  public ParameterInfo(Object instance, Object[] parameters) {
    this.instance = instance;
    this.parameters = parameters;
  }

  @Override
  public Object getInstance() {
    return instance;
  }

  @Override
  public Object[] getParameters() {
    return parameters;
  }
}
