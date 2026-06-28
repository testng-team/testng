package test.verify;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class VerifyTest extends SimpleBaseTest {

  private void runTest(Class<?> cls, int expected) {
    TestNG tng = create(cls);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();

    assertThat(tla.getPassedTests().size()).isEqualTo(expected);
  }

  @Test
  public void verifyWithAnnotation() {
    runTest(VerifySampleTest.class, 4);
  }

  @Test
  public void verifyWithoutAnnotation() {
    runTest(VerifyNoListenersSampleTest.class, 3);
  }

  @Test
  public void verifyTestListener() {
    TestNG tng = create(Verify2SampleTest.class);
    VerifyTestListener.m_count = 0;
    tng.run();
    assertThat(VerifyTestListener.m_count).isEqualTo(1);
  }

  @Test
  public void verifyBaseClassTestListener() {
    TestNG tng = create(Verify3SampleTest.class);
    VerifyTestListener.m_count = 0;
    tng.run();
    assertThat(VerifyTestListener.m_count).isEqualTo(1);
  }
}
