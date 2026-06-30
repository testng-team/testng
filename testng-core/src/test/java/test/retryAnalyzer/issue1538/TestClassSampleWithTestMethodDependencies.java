package test.retryAnalyzer.issue1538;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class TestClassSampleWithTestMethodDependencies {
  private int i = 0;

  @Test(retryAnalyzer = RetryForIssue1538.class)
  public void a() {
    assertThat(i++).isOne();
  }

  @Test(dependsOnMethods = "a", retryAnalyzer = RetryForIssue1538.class)
  public void b() {
    assertThat(i++).isEqualTo(2);
  }
}
