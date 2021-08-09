package test.configuration.sample;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;

public class VerifySuiteTestSample {

  @AfterSuite
  public void verify() {
    Assert.assertEquals(Arrays.asList(1, 2, 3), SuiteTestSample.m_order);
  }
}
