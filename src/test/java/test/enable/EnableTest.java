package test.enable;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.List;

import test.SimpleBaseTest;

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
    Assert.assertEquals(invokedMethods.get(6), "beforeSuiteC");
    Assert.assertEquals(invokedMethods.get(7), "beforeSuiteC2");
    Assert.assertEquals(invokedMethods.get(8), "beforeSuiteNoRunC");
    Assert.assertEquals(invokedMethods.get(9), "beforeSuiteNoRunC2");
    Assert.assertEquals(invokedMethods.get(10), "beforeSuiteRunC");
    Assert.assertEquals(invokedMethods.get(11), "beforeSuiteRunC2");
    Assert.assertEquals(invokedMethods.get(12), "testA2");
    Assert.assertEquals(invokedMethods.get(13), "testA3");
    Assert.assertEquals(invokedMethods.get(14), "testB2");
    Assert.assertEquals(invokedMethods.get(15), "testB3");
    Assert.assertEquals(invokedMethods.get(16), "testC");
    Assert.assertEquals(invokedMethods.get(17), "testC2");
    Assert.assertEquals(invokedMethods.get(18), "testC3");
    Assert.assertEquals(invokedMethods.size(), 19);
  }
}
