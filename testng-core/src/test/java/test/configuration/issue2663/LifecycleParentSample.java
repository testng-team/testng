package test.configuration.issue2663;

import org.testng.annotations.BeforeMethod;

/** Parent of {@link LifecycleChildSample}, carrying the highest priority of the pair. */
public class LifecycleParentSample {

  @BeforeMethod(priority = 10)
  public void parentSetup() {}
}
