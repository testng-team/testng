package test.methodselectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import test.BaseTest;
import test.methodselectors.issue1985.FilteringMethodSelector;
import test.methodselectors.issue1985.TestClassSample;

public class MethodSelectorTest extends BaseTest {

  @Test
  public void negativePriorityAllGroups() {
    addClass("test.methodselectors.SampleTest");
    addMethodSelector("test.methodselectors.AllTestsMethodSelector", -1);
    run();
    String[] passed = {
        "test1", "test2", "test3",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void negativePriorityGroup2() {
    addClass("test.methodselectors.SampleTest");
    addMethodSelector("test.methodselectors.Test2MethodSelector", -1);
    run();
    String[] passed = {
        "test2",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void lessThanPriorityTest1Test() {
    addClass("test.methodselectors.SampleTest");
    addIncludedGroup("test1");
    addMethodSelector("test.methodselectors.Test2MethodSelector", 5);
    run();
    String[] passed = {
        "test1", "test2",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void greaterThanPriorityTest1Test2() {
    addClass("test.methodselectors.SampleTest");
    addIncludedGroup("test1");
    addMethodSelector("test.methodselectors.Test2MethodSelector", 15);
    run();
    String[] passed = {
        "test2",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void lessThanPriorityAllTests() {
    addClass("test.methodselectors.SampleTest");
    addIncludedGroup("test1");
    addMethodSelector("test.methodselectors.AllTestsMethodSelector", 5);
    run();
    String[] passed = {
        "test1", "test2", "test3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test(description = "GITHUB-1507")
  public void testNoMethodsAreExecutedWithNoMatchFound() {
    String className = ClassWithManyMethodsSample.class.getName();
    addClass(className);
    addIncludedMethod(className, "cars_sedan");
    addIncludedMethod(className, "train_Local");
    addIncludedMethod(className, "flight_Domestic");
    run();
    Assert.assertTrue(getPassedTests().isEmpty());
    Assert.assertTrue(getFailedTests().isEmpty());
    Assert.assertTrue(getSkippedTests().isEmpty());
  }

  @Test(description = "GITHUB-1985")
  public void testFilteringOfMethodsWork() {
    System.setProperty(FilteringMethodSelector.GROUP, "bat");
    String className = TestClassSample.class.getName();
    addClass(className);
    addMethodSelector(FilteringMethodSelector.class.getName(), 0);
    run();
    String[] passed = {"batTest"};
    verifyTests("Passed", passed, getPassedTests());
  }
}
