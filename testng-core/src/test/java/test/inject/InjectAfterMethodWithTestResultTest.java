package test.inject;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class InjectAfterMethodWithTestResultTest extends SimpleBaseTest {

  @Test
  public void verifyTestResultInjection() {
    TestNG tng = create(InjectAfterMethodWithTestResultSampleTest.class);
    tng.run();

    assertThat(0).isEqualTo(InjectAfterMethodWithTestResultSampleTest.m_success);
  }
}
