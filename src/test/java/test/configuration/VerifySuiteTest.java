package test.configuration;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;

public class VerifySuiteTest {

  @AfterSuite
  public void verify() {
    Assert.assertEquals(Arrays.asList(1, 2, 3), SuiteTest.m_order);
  }
}
