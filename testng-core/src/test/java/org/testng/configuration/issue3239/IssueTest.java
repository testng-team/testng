package org.testng.configuration.issue3239;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.configuration.samples.issue3239.AfterMethodChildSample;
import org.testng.configuration.samples.issue3239.AfterMethodGroupsChildSample;
import org.testng.configuration.samples.issue3239.AfterSuiteAChild;
import org.testng.configuration.samples.issue3239.AfterSuiteBChild;
import org.testng.configuration.samples.issue3239.AfterTransitiveUpstreamChild;
import org.testng.configuration.samples.issue3239.AfterUnrelatedGroupsChild;
import org.testng.configuration.samples.issue3239.AlignedHardDepChild;
import org.testng.configuration.samples.issue3239.BeforeClassOrderingSample;
import org.testng.configuration.samples.issue3239.GroupChainChild;
import org.testng.configuration.samples.issue3239.MissingDependsChild;
import org.testng.configuration.samples.issue3239.MissingGroupsChild;
import org.testng.configuration.samples.issue3239.OppositeHardDepChild;
import org.testng.configuration.samples.issue3239.RegexpGroupsChild;
import org.testng.configuration.samples.issue3239.SuiteAChild;
import org.testng.configuration.samples.issue3239.SuiteBChild;
import org.testng.configuration.samples.issue3239.ThreeLevelChild;
import org.testng.configuration.samples.issue3239.TransitiveUpstreamChild;
import org.testng.configuration.samples.issue3239.UnrelatedGroupsChild;
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

  @Test(description = "GITHUB-2432")
  public void twoHierarchiesDoNotFormACycleOnBeforeSuite() {
    InvokedMethodNameListener listener = run(SuiteAChild.class, SuiteBChild.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly(
            "childB", "childBGroup", "baseA", "childA", "childAGroup", "baseB", "testA", "testB");
  }

  @Test(description = "GITHUB-2432")
  public void inheritanceEdgeDoesNotCycleForAfterClass() {
    InvokedMethodNameListener listener = run(AfterTransitiveUpstreamChild.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly("test", "parentAfter", "childDependsOnG", "childAgnostic");
  }

  @Test(description = "GITHUB-2432")
  public void inheritanceEdgeDoesNotCycleOnPureDependsOnGroupsChain() {
    InvokedMethodNameListener listener = run(GroupChainChild.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly("groupC", "childAgnostic", "childInC", "groupB", "groupA", "test");
  }

  @Test(
      expectedExceptions = TestNGException.class,
      expectedExceptionsMessageRegExp = ".*depends on nonexistent method doesNotExist")
  public void missingDependsOnMethodsStillFailsFromInheritanceWalk() {
    run(MissingDependsChild.class);
  }

  @Test(description = "GITHUB-3239")
  public void beforeClassInheritanceSurvivesUnrelatedChildGroups() {
    InvokedMethodNameListener listener = run(UnrelatedGroupsChild.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly("zSetup", "ySetup", "thisSetup", "test");
  }

  @Test(description = "GITHUB-2714")
  public void afterMethodInheritanceSurvivesUnrelatedChildGroups() {
    InvokedMethodNameListener listener = run(AfterUnrelatedGroupsChild.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly(
            "beforeMethod", "beforeChildMethod", "testCase", "afterChildMethod", "afterMethod");
  }

  @Test(description = "GITHUB-3239")
  public void alignedHardDependencyKeepsInheritanceOrder() {
    InvokedMethodNameListener listener = run(AlignedHardDepChild.class);

    assertThat(listener.getInvokedMethodNames()).containsExactly("baseSetup", "childSetup", "test");
  }

  @Test(description = "GITHUB-2432")
  public void oppositeHardDependencyWinsWithoutCycle() {
    InvokedMethodNameListener listener = run(OppositeHardDepChild.class);

    assertThat(listener.getInvokedMethodNames()).containsExactly("childSetup", "baseSetup", "test");
  }

  @Test(description = "GITHUB-2432")
  public void threeLevelInheritanceSelectivelyRejectsCyclingEdges() {
    InvokedMethodNameListener listener = run(ThreeLevelChild.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly("gpSetup", "childAgnostic", "childInG", "parentSetup", "test");
  }

  @Test(description = "GITHUB-3239")
  public void regexpDependsOnGroupsUsesSameMatchingAsExecutionGraph() {
    InvokedMethodNameListener listener = run(RegexpGroupsChild.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly("baseSetup", "childSetup", "baseAfter", "test");
  }

  @Test(description = "GITHUB-2432")
  public void twoHierarchiesDoNotFormACycleOnAfterSuite() {
    InvokedMethodNameListener listener = run(AfterSuiteAChild.class, AfterSuiteBChild.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly(
            "testA", "testB", "childA", "childAGroup", "childB", "childBGroup", "baseA", "baseB");
  }

  @Test
  public void missingDependsOnGroupsKeepsExistingErrorBehavior() {
    InvokedMethodNameListener listener = run(MissingGroupsChild.class);

    assertThat(listener.getInvokedMethodNames())
        .containsExactly("baseSetup", "childAgnostic", "test");
  }
}
