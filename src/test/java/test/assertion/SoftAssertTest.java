package test.assertion;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.Collection;

public class SoftAssertTest {
  @Test
  public void testOnSucceedCalled() throws Exception {
    final Collection<IAssert> succeed = new ArrayList<IAssert>();
    final SoftAssert sa = new SoftAssert() {
      @Override
      public void onAssertSuccess(IAssert assertCommand) {
        succeed.add(assertCommand);
      }
    };
    sa.assertTrue(true);
    sa.assertTrue(false);
    Assert.assertEquals(succeed.size(), 1, succeed.toString());
  }

  @Test
  public void testOnFailureCalled() throws Exception {
    final Collection<IAssert> failured = new ArrayList<IAssert>();
    final SoftAssert sa = new SoftAssert() {
      @Override
      public void onAssertFailure(IAssert assertCommand, AssertionError ex) {
        failured.add(assertCommand);
      }
    };
    sa.assertTrue(true);
    sa.assertTrue(false);
    Assert.assertEquals(failured.size(), 1, failured.toString());
  }

  @Test
  public void testAssertAllCount() throws Exception {
    final SoftAssert sa = new SoftAssert();
    sa.assertTrue(true);
    sa.assertTrue(false);
    try {
      sa.assertAll();
      Assert.fail("Exception expected");
    } catch (AssertionError e) {
      final String message = e.getMessage();
      Assert.assertEquals(message.split("\r?\n").length, 2, message);
    }
  }
}
