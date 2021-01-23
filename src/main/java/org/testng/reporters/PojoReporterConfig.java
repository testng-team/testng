package org.testng.reporters;

import org.testng.internal.PropertyUtils;

public class PojoReporterConfig implements IReporterConfig {

  private final Object target;

  public PojoReporterConfig(Object target) {
    this.target = target;
  }

  @Override
  public void setProperty(String name, String value) {
    PropertyUtils.setProperty(target, name, value);
  }
}
