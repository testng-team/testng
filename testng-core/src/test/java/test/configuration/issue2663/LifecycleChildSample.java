package test.configuration.issue2663;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * The priority contradicts the inheritance guarantee: the subclass asks to run first and must not
 * get it.
 */
public class LifecycleChildSample extends LifecycleParentSample {

  @BeforeMethod(priority = 0)
  public void childSetup() {}

  @Test
  public void test() {}
}
