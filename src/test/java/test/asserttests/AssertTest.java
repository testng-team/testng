package test.asserttests;

import org.testng.Assert;
import org.testng.Assert.ThrowingRunnable;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.testng.Assert.expectThrows;

public class AssertTest {

  @Test
  public void noOrderSuccess() {
    String[] rto1 = { "boolean", "BigInteger", "List",};
    String[] rto2 = {  "List", "BigInteger", "boolean",};
    Assert.assertEqualsNoOrder(rto1, rto2);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void noOrderFailure() {
    String[] rto1 = { "a", "a", "b",};
    String[] rto2 = {  "a", "b", "b",};
    Assert.assertEqualsNoOrder(rto1, rto2);
  }

  @Test
  public void intArray_Issue4() {
    int[] intArr00 = {1};
    int[] intArr01 = {1};
    Assert.assertEquals(intArr00, intArr01);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void arraysFailures_1() {
    int[] intArr = {1, 2};
    long[] longArr = {1, 2};
    Assert.assertEquals(intArr, longArr);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void arraysFailures_2() {
    int[] intArr = {1, 2};
    Assert.assertEquals(intArr, (long) 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void arraysFailures_3() {
    long[] longArr = {1};
    Assert.assertEquals((long) 1, longArr);
  }

  @Test
  public void setsSuccess() {
    Set<Integer> set1 = Sets.newHashSet();
    Set<Integer> set2 = Sets.newHashSet();

    set1.add(1);
    set2.add(1);

    set1.add(3);
    set2.add(3);

    set1.add(2);
    set2.add(2);

    Assert.assertEquals(set1, set2);
    Assert.assertEquals(set2, set1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void expectThrowsRequiresAnExceptionToBeThrown() {
    expectThrows(Throwable.class, nonThrowingRunnable());
  }

  @Test
  public void expectThrowsIncludesAnInformativeDefaultMessage() {
    try {
      expectThrows(Throwable.class, nonThrowingRunnable());
    } catch (AssertionError ex) {
      assertEquals("Expected Throwable to be thrown, but nothing was thrown", ex.getMessage());
      return;
    }
    fail();
  }

  @Test
  public void expectThrowsReturnsTheSameObjectThrown() {
    NullPointerException npe = new NullPointerException();

    Throwable throwable = expectThrows(Throwable.class, throwingRunnable(npe));

    assertSame(npe, throwable);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void expectThrowsDetectsTypeMismatchesViaExplicitTypeHint() {
    NullPointerException npe = new NullPointerException();

    expectThrows(IOException.class, throwingRunnable(npe));
  }

  @Test
  public void expectThrowsWrapsAndPropagatesUnexpectedExceptions() {
    NullPointerException npe = new NullPointerException("inner-message");

    try {
      expectThrows(IOException.class, throwingRunnable(npe));
    } catch (AssertionError ex) {
      assertSame(npe, ex.getCause());
      assertEquals("inner-message", ex.getCause().getMessage());
      return;
    }
    fail();
  }

  @Test
  public void expectThrowsSuppliesACoherentErrorMessageUponTypeMismatch() {
    NullPointerException npe = new NullPointerException();

    try {
      expectThrows(IOException.class, throwingRunnable(npe));
    } catch (AssertionError error) {
      assertEquals("Expected IOException to be thrown, but NullPointerException was thrown",
              error.getMessage());
      assertSame(npe, error.getCause());
      return;
    }
    fail();
  }

  private static ThrowingRunnable nonThrowingRunnable() {
    return new ThrowingRunnable() {
      public void run() throws Throwable {
      }
    };
  }

  private static ThrowingRunnable throwingRunnable(final Throwable t) {
    return new ThrowingRunnable() {
      public void run() throws Throwable {
        throw t;
      }
    };
  }

  @Test
  public void doubleNaNAssertion() {
    Assert.assertEquals(Double.NaN, Double.NaN, 0.0);
  }
}
