package test.listeners;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class GitHub911Sample {

  @BeforeSuite(alwaysRun = true)
  public void setUp() {
    Assert.fail();
  }
  // TODO check before invocation

  @Test
  public void myTest1() {}

  @Test
  public void myTest2() {}
}
