package test.inject;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class InjectTestResultTest extends SimpleBaseTest {

  @Test
  public void verifyTestResultInjection() {
    TestNG tng = create(InjectBeforeAndAfterMethodsWithTestResultSampleTest.class);
    tng.run();

    Assert.assertEquals(0, InjectBeforeAndAfterMethodsWithTestResultSampleTest.m_success);
  }
}
