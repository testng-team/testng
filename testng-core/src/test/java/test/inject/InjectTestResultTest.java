package test.inject;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class InjectTestResultTest extends SimpleBaseTest {

  @Test
  public void verifyTestResultInjection() {
    TestNG tng = create(InjectBeforeAndAfterMethodsWithTestResultSampleTest.class);
    tng.run();

    assertThat(InjectBeforeAndAfterMethodsWithTestResultSampleTest.m_success).isZero();
  }
}
