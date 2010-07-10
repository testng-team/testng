package test.testng195;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class AfterMethodTest extends SimpleBaseTest {

  @Test
  public void testContextShouldBeInitialized() {
    TestNG tng = create(AfterMethodSampleTest.class);
    tng.run();
    Assert.assertTrue(AfterMethodSampleTest.m_success);
  }
}
