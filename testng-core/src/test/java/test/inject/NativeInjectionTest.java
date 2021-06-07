package test.inject;

import static test.inject.NativeInjectionTestSamples.*;

import org.testng.*;
import org.testng.annotations.*;
import test.SimpleBaseTest;

public class NativeInjectionTest extends SimpleBaseTest {

  @Test(dataProvider = "getTestData")
  public void testBeforeSuiteInjection(Class clazz, String methodName, String expected) {
    TestNG tng = create(clazz);
    InjectionResultHolder holder = new InjectionResultHolder();
    tng.addListener(holder);
    tng.setGroups("test");
    tng.run();
    Assert.assertTrue(holder.getErrorMessage().contains(expected + methodName));
  }

  @DataProvider
  public Object[][] getTestData() {
    String variant1 = "Can inject only one of <ITestContext, XmlTest> into a @%s annotated ";
    String variant2 =
        "Can inject only one of <ITestContext, XmlTest, Method, Object[], ITestResult> into a @%s annotated ";
    return new Object[][] {
      {
        BadBeforeSuiteSample.class,
        "beforeSuite",
        String.format(variant1, BeforeSuite.class.getSimpleName())
      },
      {
        BadBeforeTestSample.class,
        "beforeTest",
        String.format(variant1, BeforeTest.class.getSimpleName())
      },
      {
        BadBeforeClassSample.class,
        "beforeClass",
        String.format(variant1, BeforeClass.class.getSimpleName())
      },
      {
        BadBeforeMethodSample.class,
        "beforeMethod",
        String.format(variant2, BeforeMethod.class.getSimpleName())
      },
      {
        BadAfterMethodSample.class,
        "afterMethod",
        String.format(variant2, AfterMethod.class.getSimpleName())
      },
      {
        BadAfterClassSample.class,
        "afterClass",
        String.format(variant1, AfterClass.class.getSimpleName())
      },
      {
        BadAfterTestSample.class,
        "afterTest",
        String.format(variant1, AfterTest.class.getSimpleName())
      },
      {
        BadAfterSuiteSample.class,
        "afterSuite",
        String.format(variant1, AfterSuite.class.getSimpleName())
      },
      {
        BadBeforeGroupsSample.class,
        "beforeGroups",
        String.format(variant1, BeforeGroups.class.getSimpleName())
      },
      {
        BadAfterGroupsSample.class,
        "afterGroups",
        String.format(variant1, AfterGroups.class.getSimpleName())
      }
    };
  }

  public static class InjectionResultHolder extends TestListenerAdapter {
    private String errorMessage;

    String getErrorMessage() {
      return errorMessage;
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
      this.errorMessage = itr.getThrowable().getMessage();
    }
  }
}
