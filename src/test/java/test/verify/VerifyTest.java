package test.verify;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class VerifyTest extends SimpleBaseTest {

  private void runTest(Class<?> cls, int expected) {
    TestNG tng = create(cls);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), expected);
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
    Assert.assertEquals(VerifyTestListener.m_count, 1);
  }

  @Test
  public void verifyBaseClassTestListener() {
    TestNG tng = create(Verify3SampleTest.class);
    VerifyTestListener.m_count = 0;
    tng.run();
    Assert.assertEquals(VerifyTestListener.m_count, 1);
  }

}
