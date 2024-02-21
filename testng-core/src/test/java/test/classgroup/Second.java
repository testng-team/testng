package test.classgroup;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

@Test(dependsOnGroups = {"first"})
public class Second {

  @Test
  public void verify() {
    assertTrue(First.allRun(), "Methods for class First should have been invoked first.");
  }
}
