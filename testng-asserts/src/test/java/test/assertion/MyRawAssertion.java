package test.assertion;

import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.asserts.Assertion;
import org.testng.asserts.IAssert;

public class MyRawAssertion extends Assertion {

  private final List<String> methods = new ArrayList<>();

  @Override
  public void onAssertSuccess(IAssert assertCommand) {
    methods.add("onAssertSuccess");
    super.onAssertSuccess(assertCommand);
  }

  @Override
  public void onAssertFailure(IAssert assertCommand, AssertionError ex) {
    methods.add("onAssertFailure");
    super.onAssertFailure(assertCommand, ex);
  }

  @Override
  public void onBeforeAssert(IAssert assertCommand) {
    methods.add("onBeforeAssert");
    super.onBeforeAssert(assertCommand);
  }

  @Override
  public void onAfterAssert(IAssert assertCommand) {
    methods.add("onAfterAssert");
    super.onAfterAssert(assertCommand);
  }

  public List<String> getMethods() {
    return methods;
  }

  public void myAssert(final String actual, final boolean expected, final String message) {
    doAssert(
        new IAssert() {
          @Override
          public String getMessage() {
            return message;
          }

          @Override
          public void doAssert() {
            Assert.assertNotNull(actual, message);
            Assert.assertTrue(expected, message);
          }

          @Override
          public Object getActual() {
            return actual;
          }

          @Override
          public Object getExpected() {
            return expected;
          }
        });
  }
}
