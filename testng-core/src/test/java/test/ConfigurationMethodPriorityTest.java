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
            "testB",
            "afterMethodB",
            "afterMethodA",
            "beforeMethodB",
            "beforeMethodA",
            "testA",
            "afterMethodB",
            "afterMethodA",
            "afterClassB",
            "afterClassA",
            "afterSuiteB",
            "afterSuiteA");
    String className = "test.configuration.issue2663.ConfigurationMethodPrioritySampleTest.";
    expectedLogs.replaceAll(e -> className + e);
    assertThat(ConfigurationMethodPrioritySampleTest.logs).containsExactlyElementsOf(expectedLogs);
  }

  @Test(description = "GITHUB-2663")
  public void ensureThatPriorityWorksOnConfigurationMethodsWithGroupDependency() {
    List<String> expectedOrder1 =
        Arrays.asList(
            "beforeSuite3",
            "beforeSuite2",
            "beforeSuite1",
            "beforeTest3",
            "beforeTest2",
            "beforeTest1",
            "beforeClass3",
            "beforeClass2",
            "beforeClass1",
            "beforeMethod3",
            "beforeMethod2",
            "beforeMethod1",
            "test3",
            "beforeMethod3",
            "beforeMethod2",
            "beforeMethod1",
            "test2",
            "beforeMethod3",
            "beforeMethod2",
            "beforeMethod1",
            "test1");
    TestNG testng = create(ConfigurationMethodPriorityWithGroupDependencySampleTest.class);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
    String className =
        "test.configuration.issue2663.ConfigurationMethodPriorityWithGroupDependencySampleTest.";
    expectedOrder1.replaceAll(e -> className + e);
    Assert.assertEquals(
        ConfigurationMethodPriorityWithGroupDependencySampleTest.logs, expectedOrder1);
  }

  @Test(description = "GITHUB-2663")
  public void ensureThatPriorityWorksOnConfigurationMethodsWithMethodDependency() {
    List<String> expectedOrder1 =
        Arrays.asList(
            "beforeSuite3",
            "beforeSuite2",
            "beforeSuite1",
            "beforeTest3",
            "beforeTest2",
            "beforeTest1",
            "beforeClass3",
            "beforeClass2",
            "beforeClass1",
            "beforeMethod3",
            "beforeMethod2",
            "beforeMethod1",
            "test3",
            "beforeMethod3",
            "beforeMethod2",
            "beforeMethod1",
            "test2",
            "beforeMethod3",
            "beforeMethod2",
            "beforeMethod1",
            "test1");
    TestNG testng = create(ConfigurationMethodPriorityWithMethodDependencySampleTest.class);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
    String className =
        "test.configuration.issue2663.ConfigurationMethodPriorityWithMethodDependencySampleTest.";
    expectedOrder1.replaceAll(e -> className + e);
    Assert.assertEquals(
        ConfigurationMethodPriorityWithMethodDependencySampleTest.logs, expectedOrder1);
  }

  @Test(description = "GITHUB-2663")
  public void ensureThatPriorityWorksOnConfigurationMethodsMultiLevelInheritanc() {
    String testClassName =
        "test.configuration.issue2663.ConfiguratinMethodPriorityMultiLevelInheritanceSampleTest.";
    String baseClassName1 = "test.configuration.issue2663.ConfiguratinMethodPriorityBaseClass1.";
    String baseClassName2 = "test.configuration.issue2663.ConfiguratinMethodPriorityBaseClass2.";
    List<String> expectedOrder1 =
        Arrays.asList(
            baseClassName1 + "beforeSuite3",
            baseClassName2 + "beforeSuite2",
            baseClassName2 + "beforeSuite1",
            baseClassName1 + "beforeTest3",
            baseClassName2 + "beforeTest2",
            baseClassName2 + "beforeTest1",
            baseClassName1 + "beforeClass3",
            baseClassName2 + "beforeClass2",
            baseClassName2 + "beforeClass1",
            baseClassName1 + "beforeMethod3",
            baseClassName2 + "beforeMethod2",
            baseClassName2 + "beforeMethod1",
            baseClassName1 + "test3",
            baseClassName1 + "beforeMethod3",
            baseClassName2 + "beforeMethod2",
            baseClassName2 + "beforeMethod1",
            baseClassName2 + "test2",
            baseClassName1 + "beforeMethod3",
            baseClassName2 + "beforeMethod2",
            baseClassName2 + "beforeMethod1",
            testClassName + "test1");
    TestNG testng = create(ConfiguratinMethodPriorityMultiLevelInheritanceSampleTest.class);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);

    Assert.assertEquals(
        ConfiguratinMethodPriorityMultiLevelInheritanceSampleTest.logs, expectedOrder1);
  }
}
