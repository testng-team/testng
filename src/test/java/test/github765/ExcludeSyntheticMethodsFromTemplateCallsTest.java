package test.github765;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import java.util.List;

public class ExcludeSyntheticMethodsFromTemplateCallsTest extends SimpleBaseTest {

  @Test
  public void testMethod() {
    TestNG testng = create(DuplicateCallsSample.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    List<String> methods = listener.getMethodsForTestClass(DuplicateCallsSample.class);
    Assert.assertEquals(methods.size(), 1);
    Assert.assertEquals(methods.get(0), "callExecuteTest");
  }
}
