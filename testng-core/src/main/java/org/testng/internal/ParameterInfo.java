package org.testng.internal;

public class ParameterInfo implements IParameterInfo {
  private final Object instance;
  private final int index;
  private final Object[] parameters;
  private final int invocationIndex;

  public ParameterInfo(Object instance, int index, Object[] parameters, int invocationIndex) {
    this.instance = instance;
    this.index = index;
    this.parameters = parameters;
    this.invocationIndex = invocationIndex;
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
  public int getInvocationIndex() {
    return invocationIndex;
  }

  @Override
  public Object[] getParameters() {
    return parameters;
  }
}
