package test.configuration.issue2664;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class Issue2664Test extends SimpleBaseTest {

  @Test(description = "GITHUB-2663")
  public void ensureThatConfigurationMethodsWithGroupDependencyWorks() {
    List<String> expectedOrder1 =
        Arrays.asList(
            "beforeSuite3",
            "beforeSuite1",
            "beforeSuite2",
            "beforeTest3",
            "beforeTest1",
            "beforeTest2",
            "beforeClass3",
            "beforeClass1",
            "beforeClass2",
            "beforeMethod3",
            "beforeMethod1",
            "beforeMethod2",
            "test3",
            "afterMethod3",
            "afterMethod1",
            "afterMethod2",
            "beforeMethod3",
            "beforeMethod1",
            "beforeMethod2",
            "test2",
            "afterMethod3",
            "afterMethod1",
            "afterMethod2",
            "beforeMethod3",
            "beforeMethod1",
            "beforeMethod2",
            "test1",
            "afterMethod3",
            "afterMethod1",
            "afterMethod2",
            "afterClass3",
            "afterClass1",
            "afterClass2",
            "afterTest3",
            "afterTest1",
            "afterTest2",
            "afterSuite3",
            "afterSuite1",
            "afterSuite2");
    TestNG testng = create(ConfigurationMethodWithGroupDependencySample.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
    assertThat(listener.getSucceedMethodNames()).isEqualTo(expectedOrder1);
  }

  @Test(description = "GITHUB-2663")
  public void ensureThatConfigurationMethodsWithGroupDependencyWorksWithBaseClass() {
    List<String> expectedOrder1 =
        Arrays.asList(
            "beforeSuiteBase",
            "beforeSuite3",
            "beforeSuite1",
            "beforeSuite2",
            "beforeSuiteBase2",
            "beforeTestBase",
            "beforeTest3",
            "beforeTest1",
            "beforeTest2",
            "beforeTestBase2",
            "beforeClassBase",
            "beforeClass3",
            "beforeClass1",
            "beforeClass2",
            "beforeClassBase2",
            "beforeMethodBase",
            "beforeMethod3",
            "beforeMethod1",
            "beforeMethod2",
            "beforeMethodBase2",
            "test3",
            "afterMethod3",
            "afterMethod1",
            "afterMethod2",
            "beforeMethodBase",
            "beforeMethod3",
            "beforeMethod1",
            "beforeMethod2",
            "beforeMethodBase2",
            "test2",
            "afterMethod3",
            "afterMethod1",
            "afterMethod2",
            "beforeMethodBase",
            "beforeMethod3",
            "beforeMethod1",
            "beforeMethod2",
            "beforeMethodBase2",
            "testBase",
            "afterMethod3",
            "afterMethod1",
            "afterMethod2",
            "beforeMethodBase",
            "beforeMethod3",
            "beforeMethod1",
            "beforeMethod2",
            "beforeMethodBase2",
            "test1",
            "afterMethod3",
            "afterMethod1",
            "afterMethod2",
            "afterClass3",
            "afterClass1",
            "afterClass2",
            "afterTest3",
            "afterTest1",
            "afterTest2",
            "afterSuite3",
            "afterSuite1",
            "afterSuite2");
    TestNG testng = create(ConfigurationMethodWithGroupDependencySample2.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
    assertThat(listener.getSucceedMethodNames()).isEqualTo(expectedOrder1);
  }
}
