package test.configuration.issue2663;

import org.testng.annotations.Test;

public class ConfiguratinMethodPriorityMultiLevelInheritanceSampleTest
    extends ConfiguratinMethodPriorityBaseClass2 {

  @Test(
      groups = {"g3"},
      dependsOnGroups = "g1",
      priority = 1)
  public void test1() {
    print();
  }
}
