package org.testng.internal;

public class ParameterInfo implements IParameterInfo {
  private final Object instance;
  private final int index;
  private final Object[] parameters;
  private final int currentIndex;

  public ParameterInfo(Object instance, int index, Object[] parameters, int currentIndex) {
    this.instance = instance;
    this.index = index;
    this.parameters = parameters;
    this.currentIndex = currentIndex;
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
  public int currentIndex() {
    return currentIndex;
  }

  @Override
  public Object[] getParameters() {
    return parameters;
  }
}
