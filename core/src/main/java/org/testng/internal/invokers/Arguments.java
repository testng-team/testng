package org.testng.internal.invokers;

import java.util.Map;
import org.testng.ITestNGMethod;

public class Arguments {

  protected final Object instance;
  protected final ITestNGMethod tm;
  protected final Map<String, String> params;

  protected Arguments(Object instance, ITestNGMethod tm, Map<String, String> params) {
    this.instance = instance;
    this.tm = tm;
    this.params = params;
  }

  public Object getInstance() {
    return instance;
  }

  public ITestNGMethod getTestMethod() {
    return tm;
  }

  public Map<String, String> getParameters() {
    return params;
  }
}
