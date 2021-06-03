package test.mannotation;

import org.testng.annotations.Test;

@Test(groups = "base-class", dependsOnGroups="dog3", dependsOnMethods="dom3",
    enabled=true)
public class MBase {

  public void baseTest() {}

}
