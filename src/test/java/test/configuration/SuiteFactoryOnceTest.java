package test.configuration;

import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;
import junit.framework.Assert;

public class SuiteFactoryOnceTest extends SimpleBaseTest {

  @Test
  public void suiteMethodsShouldOnlyRunOnce() {
    TestNG tng = create(SuiteFactoryOnceSample2Test.class);
    SuiteFactoryOnceSample1Test.m_before = 0;
    SuiteFactoryOnceSample1Test.m_after = 0;
    tng.run();

    Assert.assertEquals(1, SuiteFactoryOnceSample1Test.m_before);
    Assert.assertEquals(1, SuiteFactoryOnceSample1Test.m_after);
  }

}
