package test.listeners.github1130;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class GitHub1130Test extends SimpleBaseTest {

  @Test(description = "GITHUB-1130: IClassListener should only be instantiated once")
  public void classListenerShouldBeOnlyInstantiatedOnceInMultiTestContext() {
    checkGithub1130(createTests("GITHUB-1130", ASample.class, BSample.class));
  }

  @Test
  public void classListenerShouldBeOnlyInstantiatedOnce() {
    checkGithub1130(create(ASample.class, BSample.class));
  }

  private void checkGithub1130(TestNG tng) {
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener((ITestNGListener) adapter);
    tng.run();
    assertThat(adapter.getFailedTests()).isEmpty();
    assertThat(adapter.getSkippedTests()).isEmpty();
    // We cannot prevent TestNG from instantiating a listener multiple times
    // because TestNG can throw away listener instances if it finds them already registered
    // So the assertion should basically be able to check that ONLY 1 instance of the listener
    // is being used, even though TestNG created multiple instances of it.
    assertThat(MyListener.instance).isNotNull();
    assertThat(MyListener.instance.beforeSuiteCount.size()).isEqualTo(1);
    assertThat(MyListener.instance.beforeClassCount.size()).isEqualTo(2);
    assertThat(MyListener.instance.beforeSuiteCount.get(0))
        .isEqualTo(MyListener.instance.beforeClassCount.get(0))
        .isEqualTo(MyListener.instance.beforeClassCount.get(1));
  }
}
