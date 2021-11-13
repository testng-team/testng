package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class SuiteFactoryOnceTest extends SimpleBaseTest {

  @Test
  public void suiteMethodsShouldOnlyRunOnce() {
    TestNG tng = create(SuiteFactoryOnceSample2Test.class);
    SuiteFactoryOnceSample1Test.m_before = 0;
    SuiteFactoryOnceSample1Test.m_after = 0;
    tng.run();

    assertThat(SuiteFactoryOnceSample1Test.m_before).isEqualTo(1);
    assertThat(SuiteFactoryOnceSample1Test.m_after).isEqualTo(1);
  }
}
