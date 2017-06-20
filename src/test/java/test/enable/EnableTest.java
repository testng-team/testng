package test.enable;

import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EnableTest extends SimpleBaseTest {

  @Test
  public void disabled_methods_should_not_be_run() {
    TestNG tng = create(A.class, B.class, C.class);
    InvokedMethodListener listener = new InvokedMethodListener();
    tng.addListener(listener);
    tng.setPreserveOrder(true);
    tng.run();

    List<String> expected = Arrays.asList(
            "beforeSuiteA", "beforeSuiteA2", "beforeSuiteNoRunA", "beforeSuiteNoRunA2", "beforeSuiteRunA", "beforeSuiteRunA2",
            "beforeSuiteRunB", "beforeSuiteRunB2",
            "beforeSuiteC", "beforeSuiteC2", "beforeSuiteNoRunC", "beforeSuiteNoRunC2", "beforeSuiteRunC", "beforeSuiteRunC2",
            "testA2", "testA3", "testB2", "testB3", "testC", "testC2", "testC3",
            "afterSuiteA", "afterSuiteA2", "afterSuiteNoRunA", "afterSuiteNoRunA2", "afterSuiteRunA", "afterSuiteRunA2",
            "afterSuiteRunB", "afterSuiteRunB2",
            "afterSuiteC", "afterSuiteC2", "afterSuiteNoRunC", "afterSuiteNoRunC2", "afterSuiteRunC", "afterSuiteRunC2"
    );

    for (String method : listener.getInvokedMethods()) {
      assertThat(method).isIn(expected);
    }

  }

  @Test(description = "https://github.com/cbeust/testng/issues/420")
  public void issue420() {
    TestNG tng = create(Issue420FirstSample.class, Issue420SecondSample.class);
    InvokedMethodListener listener = new InvokedMethodListener();
    tng.addListener(listener);
    tng.run();

    List<String> expected = Arrays.asList(
            "alwaysBeforeSuite", "beforeSuite",
            "verifySomethingFirstSample", "verifySomethingSecondSample",
            "afterSuite", "alwaysAfterSuite"
    );

    for (String method : listener.getInvokedMethods()) {
      assertThat(method).isIn(expected);
    }

  }
}
