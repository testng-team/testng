package test.graph;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(LocalVisualiser.class)
public class TestSampleWithListener {

  @Test
  public void parent() {}

  @Test(dependsOnMethods = "parent")
  public void child() {}
}
