package test.graph;

import org.testng.annotations.Test;

public class TestSampleWithoutListener {

  @Test
  public void parent() {}

  @Test(dependsOnMethods = "parent")
  public void child() {}
}
