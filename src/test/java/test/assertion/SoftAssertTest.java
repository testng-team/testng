package test.assertion;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

public class SoftAssertTest {

  @Test
  public void testOnSucceedAndFailureCalled() {
    final Collection<IAssert> succeed = new ArrayList<>();
    final Collection<IAssert> failures = new ArrayList<>();
    final SoftAssert sa =
        new SoftAssert() {
          @Override
          public void onAssertSuccess(IAssert assertCommand) {
            succeed.add(assertCommand);
          }

          @Override
          public void onAssertFailure(IAssert assertCommand, AssertionError ex) {
            failures.add(assertCommand);
          }
        };
    sa.assertTrue(true);
    sa.assertTrue(false);
    Assert.assertEquals(succeed.size(), 1, succeed.toString());
    Assert.assertEquals(failures.size(), 1, failures.toString());
  }

  @Test
  public void testAssertAllCount() {
    String message = "My message";
    SoftAssert sa = new SoftAssert();
    sa.assertTrue(true);
    sa.assertTrue(false, message);
    try {
      sa.assertAll();
      Assert.fail("Exception expected");
    } catch (AssertionError e) {
      String[] lines = e.getMessage().split("\r?\n");
      Assert.assertEquals(lines.length, 2);
      lines[1] = lines[1].replaceFirst(message, "");
      Assert.assertFalse(lines[1].contains(message));
    }
  }

  private static final String hiddenMsg = "hidden msg";

  @Test(description = "GITHUB-1778", dataProvider = "dp")
  public void testRootCauseInSoftFail(Exception hiddenExc) {
    try {
      SoftAssert s = new SoftAssert();
      s.fail("soft assert with missing root cause", hiddenExc);
      s.assertAll();
    } catch (AssertionError soft) {
      assertThrowableContainsText(soft, hiddenMsg);
    }
  }

  @DataProvider(name = "dp")
  public Object[][] getExceptions() {
    return new Object[][] {{new Exception(hiddenMsg)}, {new Exception(new Exception(hiddenMsg))}};
  }

  private static void assertThrowableContainsText(Throwable hard, String text) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    hard.printStackTrace(pw);
    String stackTrace = sw.toString();
    Assert.assertTrue(stackTrace.contains(text));
  }
}
