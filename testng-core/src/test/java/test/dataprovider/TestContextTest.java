package test.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class TestContextTest extends SimpleBaseTest {

  @Test
  public void verifySix() {
    // Not including any group, so the two test methods should fail
    TestNG tng = create(TestContextSample.class);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getFailedMethodNames()).hasSize(2);
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {{10, "verifyTen"}, {5, "verifyFive"}};
  }

  @Test(dataProvider = "dp")
  public void verify(int number, String passed) {
    TestNG tng = create(TestContextSample.class);
    tng.setGroups(String.valueOf(number));

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).hasSize(1);
    assertThat(listener.getSucceedMethodNames().get(0))
        .matches(passed + "\\(\\[foo(,foo){" + (number - 1) + "}?\\]\\)");
    assertThat(listener.getFailedMethodNames()).isEmpty();
  }
}
