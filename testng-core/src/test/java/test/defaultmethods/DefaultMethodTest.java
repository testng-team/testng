package test.defaultmethods;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(testMethods.length).isOne();
    assertThat(testMethods[0].getMethodName()).isEqualTo("defaultMethodTest");
  }

  @Test(
      description = "Before class default methods defined in an interface should be run",
      dataProvider = "classes")
  public void beforeClassDefaultShouldRun(Class<?> clazz) {
    ITestClass testClass = runTestWithDefaultMethods(clazz);

    ITestNGMethod[] beforeClassMethods = testClass.getBeforeClassMethods();
    assertThat(beforeClassMethods.length).isOne();
    assertThat(beforeClassMethods[0].getMethodName()).isEqualTo("beforeClassRun");
  }

  @Test(
      description = "After class default methods defined in an interface should be run",
      dataProvider = "classes")
  public void afterClassDefaultShouldRun(Class<?> clazz) {
    ITestClass testClass = runTestWithDefaultMethods(clazz);

    ITestNGMethod[] afterClassMethods = testClass.getAfterClassMethods();
    assertThat(afterClassMethods.length).isOne();
    assertThat(afterClassMethods[0].getMethodName()).isEqualTo("afterClassRun");
  }

  @Test(
      description = "Before method default methods defined in an interface should be run",
      dataProvider = "classes")
  public void beforeMethodDefaultShouldRun(Class<?> clazz) {
    final ITestClass testClass = runTestWithDefaultMethods(clazz);

    ITestNGMethod[] beforeMethods = testClass.getBeforeTestMethods();
    assertThat(beforeMethods.length).isOne();
    assertThat(beforeMethods[0].getMethodName()).isEqualTo("beforeMethodRun");
  }

  @Test(
      description = "After method default methods defined in an interface should be run",
      dataProvider = "classes")
  public void afterMethodDefaultShouldRun(Class<?> clazz) {
    final ITestClass testClass = runTestWithDefaultMethods(clazz);

    ITestNGMethod[] afterMethods = testClass.getAfterTestMethods();
    assertThat(afterMethods.length).isOne();
    assertThat(afterMethods[0].getMethodName()).isEqualTo("afterMethodRun");
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
