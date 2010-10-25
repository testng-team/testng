package test.inject;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class InjectAfterMethodWithTestResultTest extends SimpleBaseTest {

  @Test
  public void verifyTestResultInjection() {
    TestNG tng = create(InjectAfterMethodWithTestResultSampleTest.class);
    tng.run();

    Assert.assertEquals(0, InjectAfterMethodWithTestResultSampleTest.m_success);
  }
}
