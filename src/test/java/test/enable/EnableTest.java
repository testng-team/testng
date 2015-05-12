package test.enable;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.List;

import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class EnableTest extends SimpleBaseTest {

  @Test
  public void disabled_methods_should_not_be_run() {
    TestNG tng = create(A.class, B.class, C.class);
    InvokedMethodListener listener = new InvokedMethodListener();
    tng.addListener(listener);
    tng.setPreserveOrder(true);
    tng.run();

    List<String> invokedMethods = listener.getInvokedMethods();
    Assert.assertEquals(invokedMethods.get(0), "beforeSuiteA");
    Assert.assertEquals(invokedMethods.get(1), "beforeSuiteA2");
    Assert.assertEquals(invokedMethods.get(2), "beforeSuiteNoRunA");
    Assert.assertEquals(invokedMethods.get(3), "beforeSuiteNoRunA2");
    Assert.assertEquals(invokedMethods.get(4), "beforeSuiteRunA");
    Assert.assertEquals(invokedMethods.get(5), "beforeSuiteRunA2");
    Assert.assertEquals(invokedMethods.get(6), "beforeSuiteRunB");
    Assert.assertEquals(invokedMethods.get(7), "beforeSuiteRunB2");
    Assert.assertEquals(invokedMethods.get(8), "beforeSuiteC");
    Assert.assertEquals(invokedMethods.get(9), "beforeSuiteC2");
    Assert.assertEquals(invokedMethods.get(10), "beforeSuiteNoRunC");
    Assert.assertEquals(invokedMethods.get(11), "beforeSuiteNoRunC2");
    Assert.assertEquals(invokedMethods.get(12), "beforeSuiteRunC");
    Assert.assertEquals(invokedMethods.get(13), "beforeSuiteRunC2");
    Assert.assertEquals(invokedMethods.get(14), "testA2");
    Assert.assertEquals(invokedMethods.get(15), "testA3");
    Assert.assertEquals(invokedMethods.get(16), "testB2");
    Assert.assertEquals(invokedMethods.get(17), "testB3");
    Assert.assertEquals(invokedMethods.get(18), "testC");
    Assert.assertEquals(invokedMethods.get(19), "testC2");
    Assert.assertEquals(invokedMethods.get(20), "testC3");
    Assert.assertEquals(invokedMethods.size(), 21);
  }

  @Test(description = "https://github.com/cbeust/testng/issues/420")
  public void issue420() {
    TestNG tng = create(Issue420FirstSample.class, Issue420SecondSample.class);
    InvokedMethodListener listener = new InvokedMethodListener();
    tng.addListener(listener);
    tng.run();

    assertThat(listener.getInvokedMethods()).containsExactly(
        "initEnvironment",
        "verifySomethingFirstSample", "verifySomethingSecondSample"
    );
  }
}
