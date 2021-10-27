package test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.configuration.issue2663.ConfiguratinMethodPriorityMultiLevelInheritanceSampleTest;
import test.configuration.issue2663.ConfigurationMethodPrioritySampleTest;
import test.configuration.issue2663.ConfigurationMethodPriorityWithGroupDependencySampleTest;
import test.configuration.issue2663.ConfigurationMethodPriorityWithMethodDependencySampleTest;

public class ConfigurationMethodPriorityTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2663")
  public void ensureThatPriorityWorksOnConfigurationMethods() {
    TestNG testng = create(ConfigurationMethodPrioritySampleTest.class);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
    List<String> expectedLogs =
        Arrays.asList(
            "beforeSuiteB",
            "beforeSuiteA",
            "beforeClassB",
            "beforeClassA",
            "beforeMethodB",
            "beforeMethodA",
            "TestB",
            "afterMethodB",
            "afterMethodA",
            "beforeMethodB",
            "beforeMethodA",
            "TestA",
            "afterMethodB",
            "afterMethodA",
            "afterClassB",
            "afterClassA",
            "afterSuiteB",
            "afterSuiteA");
    assertThat(ConfigurationMethodPrioritySampleTest.logs).containsExactlyElementsOf(expectedLogs);
  }

  @Test(description = "GITHUB-2663")
  public void ensureThatPriorityWorksOnConfigurationMethodsWithGroupDependency() {
    List<String> expectedOrder1 =
        Arrays.asList(
            "s3", "s2", "s1", "t3", "t2", "t1", "c3", "c2", "c1", "m3", "m2", "m1", "test3", "m3",
            "m2", "m1", "test2", "m3", "m2", "m1", "test1");
    TestNG tng = create(ConfigurationMethodPriorityWithGroupDependencySampleTest.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();
    System.out.println(expectedOrder1);
    System.out.println(listener.getSucceedMethodNames());
    Assert.assertEquals(listener.getSucceedMethodNames(), expectedOrder1);
  }

  @Test(description = "GITHUB-2663")
  public void ensureThatPriorityWorksOnConfigurationMethodsWithMethodDependency() {
    List<String> expectedOrder1 =
        Arrays.asList(
            "s3", "s2", "s1", "t3", "t2", "t1", "c3", "c2", "c1", "m3", "m2", "m1", "test3", "m3",
            "m2", "m1", "test2", "m3", "m2", "m1", "test1");
    TestNG tng = create(ConfigurationMethodPriorityWithMethodDependencySampleTest.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();
    System.out.println(expectedOrder1);
    System.out.println(listener.getSucceedMethodNames());
    Assert.assertEquals(listener.getSucceedMethodNames(), expectedOrder1);
  }

  @Test(description = "GITHUB-2663")
  public void ensureThatPriorityWorksOnConfigurationMethodsMultiLevelInheritanc() {
    List<String> expectedOrder1 =
        Arrays.asList(
            "s3", "s2", "s1", "t3", "t2", "t1", "c3", "c2", "c1", "m3", "m2", "m1", "test3", "m3",
            "m2", "m1", "test2", "m3", "m2", "m1", "test1");
    TestNG tng = create(ConfiguratinMethodPriorityMultiLevelInheritanceSampleTest.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();
    System.out.println(expectedOrder1);
    System.out.println(listener.getSucceedMethodNames());
    Assert.assertEquals(listener.getSucceedMethodNames(), expectedOrder1);
  }
}
