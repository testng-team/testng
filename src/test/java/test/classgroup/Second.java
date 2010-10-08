package test.classgroup;

import org.testng.annotations.Test;

@Test(dependsOnGroups = { "first" })
public class Second {

  @Test
  public void verify() {
    assert First.allRun() : "Methods for class First should have been invoked first.";
  }

}