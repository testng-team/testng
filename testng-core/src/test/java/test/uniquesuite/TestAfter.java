package test.uniquesuite;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.testng.testhelper.OutputDirectoryPatch;

public class TestAfter {

  @Test
  public void testAfter() {
    TestNG tng = new TestNG();
    tng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    tng.setTestClasses(new Class[] {TestAfter1.class, TestAfter2.class});
    tng.run();
    assertThat(BaseAfter.m_afterCount).isEqualTo(1);
  }

  @AfterTest
  public void afterTest() {
    BaseAfter.m_afterCount = 0;
    BaseBefore.m_beforeCount = 0;
    BaseBefore.m_afterCount = 0;
  }
}

/////
