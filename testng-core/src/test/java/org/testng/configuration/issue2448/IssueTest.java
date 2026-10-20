package org.testng.configuration.issue2448;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/**
 * GITHUB-2448. A configuration method with {@code alwaysRun = true} and {@code dependsOnMethods} is
 * skipped when a method it depends on fails, and its dependencies still set the order.
 */
public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2448")
  public void alwaysRunTeardownIsSkippedWhenItsDependencyFails() {
    AfterSample.invoked.clear();
    RecordingConfigListener listener = run(AfterSample.class);

    assertThat(AfterSample.invoked).containsExactly("test", "saveScreenshot");
    assertThat(listener.failed).containsExactly("saveScreenshot");
    assertThat(listener.skipped).containsExactly("uploadScreenshot");
  }

  @Test(description = "GITHUB-2448")
  public void alwaysRunTeardownStillRunsPastAnUnrelatedFailure() {
    AfterDependencyPassesSample.invoked.clear();
    RecordingConfigListener listener = run(AfterDependencyPassesSample.class);

    assertThat(AfterDependencyPassesSample.invoked)
        .containsExactly("createDriver", "saveScreenshot", "uploadScreenshot");
    assertThat(listener.failed).containsExactly("createDriver");
    assertThat(listener.passed).containsExactly("saveScreenshot", "uploadScreenshot");
  }

  @Test(description = "GITHUB-2448")
  public void alwaysRunSetupIsSkippedWhenItsDependencyFails() {
    SameClassSample.invoked.clear();
    RecordingConfigListener listener = run(SameClassSample.class);

    assertThat(SameClassSample.invoked).containsExactly("createDriver");
    assertThat(listener.failed).containsExactly("createDriver");
    assertThat(listener.skipped).containsExactly("openPages");
  }

  @Test(description = "GITHUB-2448")
  public void alwaysRunSetupIsSkippedWhenItsInheritedDependencyFails() {
    BaseSample.invoked.clear();
    RecordingConfigListener listener = run(ChildSample.class);

    assertThat(BaseSample.invoked).containsExactly("createDriver");
    assertThat(listener.failed).containsExactly("createDriver");
    assertThat(listener.skipped).containsExactly("openPages");
  }

  @Test(description = "GITHUB-2448")
  public void alwaysRunSetupIsSkippedWhenItsDependencyFailsUnderAGroupFilter() {
    GroupsSample.invoked.clear();
    TestNG testng = create(GroupsSample.class);
    testng.setGroups("smoke");
    RecordingConfigListener listener = new RecordingConfigListener();
    testng.addListener(listener);
    testng.run();

    assertThat(GroupsSample.invoked).containsExactly("createDriver");
    assertThat(listener.skipped).containsExactly("openPages");
  }

  @Test(description = "GITHUB-2448")
  public void dependsOnMethodsSetsTheOrderOfAlwaysRunConfigurations() {
    OrderSample.invoked.clear();
    run(OrderSample.class);

    assertThat(OrderSample.invoked)
        .containsExactly(
            "zCreateDriver", "aOpenPages", "test", "zSaveScreenshot", "aUploadScreenshot");
  }

  private static RecordingConfigListener run(Class<?> sample) {
    TestNG testng = create(sample);
    RecordingConfigListener listener = new RecordingConfigListener();
    testng.addListener(listener);
    testng.run();
    return listener;
  }
}
