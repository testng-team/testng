package test.assertion;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.Collection;

public class SoftAssertTest {

  @Test
  public void testOnSucceedAndFailureCalled() throws Exception {
    final Collection<IAssert> succeed = new ArrayList<>();
    final Collection<IAssert> failures = new ArrayList<>();
    final SoftAssert sa = new SoftAssert() {
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
  public void testAssertAllCount() throws Exception {
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
}
