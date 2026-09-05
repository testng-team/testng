package test.configuration.issue3239;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3239")
  public void beforeClassInheritanceSurvivesGroupDependenciesOnBaseMethods() {
    InvokedMethodNameListener listener = run(BeforeClassOrderingSample.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly("zSetup", "ySetup", "thisSetup", "test");
  }

  @Test(description = "GITHUB-2714")
  public void afterMethodInheritanceSurvivesGroupDependenciesOnBaseMethods() {
    InvokedMethodNameListener listener = run(AfterMethodChildSample.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly(
            "beforeMethod", "beforeChildMethod", "testCase", "afterChildMethod", "afterMethod");
  }

  @Test(description = "GITHUB-2714")
  public void afterMethodInheritanceSurvivesGroupsAndDependsOnGroups() {
    InvokedMethodNameListener listener = run(AfterMethodGroupsChildSample.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly(
            "beforeMethod", "beforeChildMethod", "testCase", "afterChildMethod", "afterMethod");
  }

  @Test(description = "GITHUB-2432")
  public void inheritanceEdgeDoesNotCycleWhenAgnosticMethodIsTransitivelyUpstream() {
    InvokedMethodNameListener listener = run(TransitiveUpstreamChild.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly("baseGroup", "childAgnostic", "childGroup", "baseAfterGroup", "test");
  }
}
