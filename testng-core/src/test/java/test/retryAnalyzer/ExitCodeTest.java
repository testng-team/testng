package test.retryAnalyzer;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class ExitCodeTest extends SimpleBaseTest {
  @Test
  public void exitsWithZeroOnSuccess() {
    TestNG tng = create(ImmediateSuccess.class);
    tng.run();
    assertThat(tng.getStatus()).isZero();
  }

  @Test
  public void exitsWithNonzeroOnFailure() {
    TestNG tng = create(PersistentFailure.class);
    tng.run();
    assertThat(tng.getStatus()).isNotEqualTo(0);
  }

  @Test
  public void exitsWithZeroAfterSuccessfulRetry() {
    TestNG tng = create(EventualSuccess.class);
    tng.addListener((ITestNGListener) new TestResultPruner());
    tng.run();
    assertThat(tng.getStatus()).isZero();
  }

  @Test(description = "GITHUB-217")
  public void exitWithNonzeroOnSkips() {
    TestNG tng = create(Issue217TestClassSample.class);
    tng.run();
    assertThat(tng.getStatus()).isEqualTo(2);
  }

  @Test(description = "GITHUB-217")
  public void exitWithNonzeroOnSkips1() {
    TestNG tng = create(Issue217TestClassSampleWithOneDataProvider.class);
    tng.run();
    assertThat(tng.getStatus()).isEqualTo(2);
  }
}
