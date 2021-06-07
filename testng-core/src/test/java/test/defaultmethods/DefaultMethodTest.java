package test.defaultmethods;

import org.testng.Assert;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.listeners.TestAndClassListener;

public class DefaultMethodTest extends SimpleBaseTest {

  @DataProvider
  public Object[][] classes() {
    return new Object[][] {
      new Object[] {TestA.class}, new Object[] {TestB.class}, new Object[] {TestC.class}
    };
  }

  @Test(
      description = "Test default methods defined in an interface should be run",
      dataProvider = "classes")
  public void testDefaultShouldRun(Class<?> clazz) {
    ITestClass testClass = runTestWithDefaultMethods(clazz);

    ITestNGMethod[] testMethods = testClass.getTestMethods();
    Assert.assertEquals(testMethods.length, 1);
    Assert.assertEquals(testMethods[0].getMethodName(), "defaultMethodTest");
  }

  @Test(
      description = "Before class default methods defined in an interface should be run",
      dataProvider = "classes")
  public void beforeClassDefaultShouldRun(Class<?> clazz) {
    ITestClass testClass = runTestWithDefaultMethods(clazz);

    ITestNGMethod[] beforeClassMethods = testClass.getBeforeClassMethods();
    Assert.assertEquals(beforeClassMethods.length, 1);
    Assert.assertEquals(beforeClassMethods[0].getMethodName(), "beforeClassRun");
  }

  @Test(
      description = "After class default methods defined in an interface should be run",
      dataProvider = "classes")
  public void afterClassDefaultShouldRun(Class<?> clazz) {
    ITestClass testClass = runTestWithDefaultMethods(clazz);

    ITestNGMethod[] afterClassMethods = testClass.getAfterClassMethods();
    Assert.assertEquals(afterClassMethods.length, 1);
    Assert.assertEquals(afterClassMethods[0].getMethodName(), "afterClassRun");
  }

  @Test(
      description = "Before method default methods defined in an interface should be run",
      dataProvider = "classes")
  public void beforeMethodDefaultShouldRun(Class<?> clazz) {
    final ITestClass testClass = runTestWithDefaultMethods(clazz);

    ITestNGMethod[] beforeMethods = testClass.getBeforeTestMethods();
    Assert.assertEquals(beforeMethods.length, 1);
    Assert.assertEquals(beforeMethods[0].getMethodName(), "beforeMethodRun");
  }

  @Test(
      description = "After method default methods defined in an interface should be run",
      dataProvider = "classes")
  public void afterMethodDefaultShouldRun(Class<?> clazz) {
    final ITestClass testClass = runTestWithDefaultMethods(clazz);

    ITestNGMethod[] afterMethods = testClass.getAfterTestMethods();
    Assert.assertEquals(afterMethods.length, 1);
    Assert.assertEquals(afterMethods[0].getMethodName(), "afterMethodRun");
  }

  private ITestClass runTestWithDefaultMethods(Class<?> clazz) {
    TestNG tng = create(clazz);
    TestClassListener listener = new TestClassListener();
    tng.addListener(listener);
    tng.run();
    return listener.testClass;
  }

  public static class TestClassListener extends TestAndClassListener {

    private ITestClass testClass;

    @Override
    public void onBeforeClass(ITestClass testClass) {
      this.testClass = testClass;
    }
  }
}
