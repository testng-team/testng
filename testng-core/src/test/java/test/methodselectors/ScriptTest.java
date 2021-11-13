package test.methodselectors;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.BaseTest;

public class ScriptTest extends BaseTest {

  @DataProvider
  public static Object[][] dataProvider() {
    return new Object[][] {
      new Object[] {
        "beanshell", "groups.\n     containsKey   \t    (\"test1\")", new String[] {"test1"}
      },
      new Object[] {
        "groovy", "groups.\n     containsKey   \t    (\"test2\")", new String[] {"test2"}
      },
    };
  }

  @Test(dataProvider = "dataProvider")
  public void onlyGroup1(String language, String expression, String[] passed) {
    addClass(test.methodselectors.SampleTest.class);
    setScript(language, expression);
    run();
    String[] failed = {};
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }
}
