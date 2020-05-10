package org.testng;

@Deprecated
public class ReporterConfig {

  // Backward compatible: used by Surefire and custom reporters
  public static org.testng.internal.ReporterConfig deserialize(String inputString) {
    return org.testng.internal.ReporterConfig.deserialize(inputString);
  }
}
