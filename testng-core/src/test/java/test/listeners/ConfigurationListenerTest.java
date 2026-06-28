package test.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.IConfigurationListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class ConfigurationListenerTest extends SimpleBaseTest {

  public static class Issue3166Sample {

    @BeforeClass
    public void firstBeforeClass() {
      throw new SkipException("Skip");
    }

    @BeforeClass(dependsOnMethods = "firstBeforeClass")
    public void secondBeforeClass() {}

    @Test
    public void test() {}
  }

  public static class CL implements IConfigurationListener {

    private static int m_status = 0;
    private Throwable throwable;

    @Override
    public void beforeConfiguration(ITestResult tr) {
      m_status += 1;
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
      m_status += 3;
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
      m_status += 5;
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
      m_status += 7;
      if (itr.getMethod().getMethodName().equals("secondBeforeClass")) {
        throwable = itr.getThrowable();
      }
    }
  }

  private void runTest(Class<?> cls, int expected) {
    TestNG tng = create(cls);
    CL listener = new CL();
    CL.m_status = 0;
    tng.addListener(listener);
    tng.run();

    assertThat(CL.m_status).isEqualTo(expected);
  }

  @Test
  public void shouldSucceed() {
    runTest(ConfigurationListenerSucceedSampleTest.class, 1 + 3);
  }

  @Test
  public void shouldFail() {
    runTest(ConfigurationListenerFailSampleTest.class, 1 + 5);
  }

  @Test
  public void shouldSkip() {
    runTest(ConfigurationListenerSkipSampleTest.class, 1 + 1 + 5 + 7); // fail + skip
  }

  @Test(description = "github 3166")
  public void skippedConfigurationShouldHaveThrowable() {
    TestNG tng = create(Issue3166Sample.class);
    CL listener = new CL();
    tng.addListener(listener);
    tng.run();

    assertThat(listener.throwable).isNotNull();
    assertThat((listener.throwable instanceof SkipException)).isTrue();
    assertThat(listener.throwable.getMessage()).isEqualTo("Skip");
  }
}
