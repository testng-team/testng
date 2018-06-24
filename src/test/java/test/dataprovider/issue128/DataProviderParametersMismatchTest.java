package test.dataprovider.issue128;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.reflect.MethodMatcherException;
import test.SimpleBaseTest;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class DataProviderParametersMismatchTest extends SimpleBaseTest {

  @Test
  public void testIfWarningsAreServed() {
    PrintStream currentErr = System.err;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintStream stream = new PrintStream(out);
      System.setErr(stream);
      TestNG tng = create(GitHub128Sample.class);
      tng.run();
      String msg = new String(out.toByteArray(), StandardCharsets.UTF_8);
      boolean contains =
          msg.contains(
              "Missing one or more parameters that are being injected by the data provider. Please add the below arguments to the method.");
      Assert.assertTrue(contains, "Missing parameters warning should have triggered");
    } finally {
      System.setErr(currentErr);
      System.setProperty("strictParameterMatch", "false");
    }
  }

  @Test(dependsOnMethods = "testIfWarningsAreServed")
  public void testIfExceptionIsRaised() {
    try {
      System.setProperty("strictParameterMatch", "true");
      TestNG tng = create(GitHub128Sample.class);
      TestListenerAdapter listener = new TestListenerAdapter();
      tng.addListener(listener);
      tng.run();
      for (ITestResult each : listener.getFailedTests()) {
        Assert.assertTrue(each.getThrowable() instanceof MethodMatcherException);
      }
      Assert.assertEquals(listener.getFailedTests().size(), 2);
    } finally {
      System.setProperty("strictParameterMatch", "false");
    }
  }
}
