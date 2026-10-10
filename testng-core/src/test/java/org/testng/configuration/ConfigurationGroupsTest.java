package org.testng.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.configuration.samples.ConfigurationGroups1SampleTest;
import org.testng.configuration.samples.ConfigurationGroups2SampleTest;
import org.testng.configuration.samples.ConfigurationGroups3SampleTest;
import org.testng.configuration.samples.ConfigurationGroups4SampleTest;
import org.testng.configuration.samples.ConfigurationGroups5SampleTest;
import org.testng.configuration.samples.ConfigurationGroups6SampleTest;
import org.testng.configuration.samples.ConfigurationGroups7SampleTest;
import org.testng.configuration.samples.MultipleBeforeGroupTest;
import org.testng.configuration.samples.issue2664.MethodInvocationListener;
import test.SimpleBaseTest;

public class ConfigurationGroupsTest extends SimpleBaseTest {

  @Test
  public void multipleBeforeGroupTest() {
    TestNG testng = create(MultipleBeforeGroupTest.class);
    testng.setGroups("foo");
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
  }

  @Test(dataProvider = "getTestData")
  public void runTest(Class<?> testClass, String groupName) {
    TestNG testng = create(testClass);
    testng.setGroups(groupName);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
  }

  @DataProvider
  public Object[][] getTestData() {
    return new Object[][] {
      {ConfigurationGroups1SampleTest.class, "cg1-a, cg1-1"},
      {ConfigurationGroups2SampleTest.class, "cg2-a,cg2-1"},
      {ConfigurationGroups3SampleTest.class, "cg34-a, cg34-1"},
      {ConfigurationGroups4SampleTest.class, "cg4-1"},
      {ConfigurationGroups5SampleTest.class, "cg5-1, cg5-2"},
      {ConfigurationGroups6SampleTest.class, "cg6-1"},
      {ConfigurationGroups7SampleTest.class, "A"}
    };
  }

  @Test(description = "GITHUB-2664", dataProvider = "getTestClasses")
  public void ensureDependsOnGroupsHonouredForConfigs(Class<?> clazz) {
    TestNG testng = create(clazz);
    testng.setVerbose(2);
    MethodInvocationListener listener = new MethodInvocationListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.logs).containsExactly("s3", "s1", "s2", "test3");
  }

  @DataProvider(name = "getTestClasses")
  public Object[][] getTestClasses() {
    return new Object[][] {
      {org.testng.configuration.samples.issue2664.suite.GroupDependenciesSample.class},
      {org.testng.configuration.samples.issue2664.suite.GroupDependenciesChildSample.class},
      {org.testng.configuration.samples.issue2664.test.GroupDependenciesSample.class},
      {org.testng.configuration.samples.issue2664.test.GroupDependenciesChildSample.class},
      {org.testng.configuration.samples.issue2664.cls.GroupDependenciesSample.class},
      {org.testng.configuration.samples.issue2664.cls.GroupDependenciesChildSample.class},
    };
  }
}
