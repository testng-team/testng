package org.testng;

import org.testng.collections.Lists;

@Deprecated
// TODO remove and update org.testng.internal.ReporterConfig
public class ReporterConfig extends org.testng.internal.ReporterConfig {

  public ReporterConfig() {
    super(null, Lists.newArrayList());
  }

  public void addProperty(Property property) {
    properties.add(property);
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  // Backward compatible: used by Surefire and custom reporters
  public static ReporterConfig deserialize(String inputString) {
    org.testng.internal.ReporterConfig config = org.testng.internal.ReporterConfig.deserialize(inputString);
    if (config == null) {
      return null;
    }
    ReporterConfig result = new ReporterConfig();
    result.className = config.getClassName();
    result.properties.addAll(config.getProperties());
    return result;
  }
}
