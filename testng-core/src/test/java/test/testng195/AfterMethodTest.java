package test.testng195;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class AfterMethodTest extends SimpleBaseTest {

  @Test
  public void testContextShouldBeInitialized() {
    TestNG tng = create(AfterMethodSampleTest.class);
    tng.run();
    assertThat(AfterMethodSampleTest.m_success).isTrue();
  }
}
