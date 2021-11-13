package test.listeners.github1130;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
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
    MyListener.count = 0;
    MyListener.beforeSuiteCount = new ArrayList<>();
    MyListener.beforeClassCount = new ArrayList<>();
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener((ITestNGListener) adapter);
    tng.run();
    assertThat(adapter.getFailedTests()).isEmpty();
    assertThat(adapter.getSkippedTests()).isEmpty();
    assertThat(MyListener.beforeSuiteCount.size()).isEqualTo(1);
    assertThat(MyListener.beforeClassCount.size()).isEqualTo(2);
    assertThat(MyListener.beforeSuiteCount.get(0))
        .isEqualTo(MyListener.beforeClassCount.get(0))
        .isEqualTo(MyListener.beforeClassCount.get(1));
    assertThat(MyListener.count).isEqualTo(1);
  }
}
