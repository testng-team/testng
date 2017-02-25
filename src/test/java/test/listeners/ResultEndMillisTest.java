package test.listeners;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;
import test.sample.Sample1;

public class ResultEndMillisTest extends SimpleBaseTest {

  @Test
  public void endMillisShouldBeNonNull() {
    TestNG tng = create(Sample1.class);
    tng.addListener((ITestNGListener) new ResultListener());
    tng.run();

    Assert.assertTrue(ResultListener.m_end > 0);
  }
}
