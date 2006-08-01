package test.dependent;

import org.testng.annotations.Test;

public class MissingGroupSampleTest {

  @Test(dependsOnGroups = {"missing-group"})
  public void shouldBeSkipped() {
    
  }
}
