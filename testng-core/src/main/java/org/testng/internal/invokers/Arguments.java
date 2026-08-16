package org.testng.internal.invokers;

import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.testng.ITestNGMethod;

public class Arguments {

  protected final @Nullable Object instance;
  protected final @Nullable ITestNGMethod tm;
  protected final Map<String, String> params;

  protected Arguments(
      @Nullable Object instance, @Nullable ITestNGMethod tm, Map<String, String> params) {
    this.instance = instance;
    this.tm = tm;
    this.params = params;
  }

  public @Nullable Object getInstance() {
    return instance;
  }

  public @Nullable ITestNGMethod getTestMethod() {
    return tm;
  }

  public Map<String, String> getParameters() {
    return params;
  }
}
