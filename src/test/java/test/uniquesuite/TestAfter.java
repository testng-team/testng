package test.uniquesuite;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import testhelper.OutputDirectoryPatch;

public class TestAfter {

  @Test
  public void testAfter() {
    TestNG tng = new TestNG();
    tng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    tng.setTestClasses(new Class[] { TestAfter1.class, TestAfter2.class });
    tng.setVerbose(0);
    tng.run();
    Assert.assertEquals(BaseAfter.m_afterCount, 1);
  }

  @AfterTest
  public void afterTest() {
    BaseAfter.m_afterCount = 0;
    BaseBefore.m_beforeCount = 0;
    BaseBefore.m_afterCount = 0;
  }
}

/////

