package test.inheritance.github949;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class Github949Test extends SimpleBaseTest {
  @Test(dataProvider = "getdata")
  public void runTest(Class<?> child) {
    TestNG testng = create(child);
    testng.run();
    String prefix = child.getName() + ".";
    assertThat(CommonBaseClass.messages)
        .containsExactly(prefix + "independent", prefix + "dependent");
  }

  @AfterMethod
  public void cleanup() {
    CommonBaseClass.messages.clear();
  }

  @DataProvider
  public Object[][] getdata() {
    return new Object[][] {{ChildClassSample.class}, {ChildClassWithAlwasyRunEnabledSample.class}};
  }
}
