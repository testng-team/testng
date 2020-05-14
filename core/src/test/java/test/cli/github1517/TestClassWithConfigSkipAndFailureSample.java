package test.cli.github1517;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestClassWithConfigSkipAndFailureSample {
  @BeforeClass
  public void beforeClass() {
    throw new RuntimeException("Simulating a configuration failure");
  }

  @BeforeMethod
  public void beforeMethod() {
    // Intentionally left empty
  }

  @Test
  public void testMethod() {
    Assert.assertTrue(true);
  }
}
