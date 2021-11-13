package test.configuration.issue2432;

import org.testng.annotations.BeforeSuite;

public class Base {
  @BeforeSuite(groups = "prepareConfig")
  public void prepareConfig() {}

  @BeforeSuite(groups = "uploadConfigToDatabase", dependsOnGroups = "prepareConfig")
  public void uploadConfigToDatabase() {}

  @BeforeSuite(
      groups = "verifyConfigurationAfterInstall",
      dependsOnGroups = "uploadConfigToDatabase")
  public void verifyConfigurationAfterInstall() {}
}
