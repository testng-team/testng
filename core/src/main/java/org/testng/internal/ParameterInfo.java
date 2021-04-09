package org.testng.internal;

public class ParameterInfo implements IParameterInfo {
  private Object instance;
  private final int index;
  private Object[] parameters;

  public ParameterInfo(Object instance, int index, Object[] parameters) {
    this.instance = instance;
    this.index = index;
    this.parameters = parameters;
  }

  @Override
  public Object getInstance() {
    return instance;
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public Object[] getParameters() {
    return parameters;
  }
}
