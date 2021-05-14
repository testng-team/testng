package test.asserttests;

import org.testng.Assert;
import org.testng.Assert.ThrowingRunnable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

import java.io.IOException;
import java.util.Set;

import static org.testng.Assert.*;

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
    assertEquals(intArr00, intArr01);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void arraysFailures_1() {
    int[] intArr = {1, 2};
    long[] longArr = {1, 2};
    assertEquals(intArr, longArr);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void arraysFailures_2() {
    int[] intArr = {1, 2};
    assertEquals(intArr, (long) 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void arraysFailures_3() {
    long[] longArr = {1};
    assertEquals((long) 1, longArr);
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

    assertEquals(set1, set2);
    assertEquals(set2, set1);
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
    return () -> {
    };
  }

  private static ThrowingRunnable throwingRunnable(final Throwable t) {
    return () -> {
      throw t;
    };
  }

  @Test
  public void doubleNaNAssertion() {
    assertEquals(Double.NaN, Double.NaN, 0.0);
  }

  @DataProvider
  public Object[][] identicalArraysWithNull() {
    return new Object[][]{
            { new String[] { "foo", "bar", null}, new String[] { "foo", "bar", null}},
            { new String[] { "foo", null, "bar"}, new String[] { "foo", null, "bar"}},
            { new String[] { null, "foo", "bar"}, new String[] { null, "foo", "bar"}}
    };
  }

  @Test(dataProvider="identicalArraysWithNull")
  public void identicalArraysWithNullValues(String[] actual, String[] expected) {
    assertEquals(actual, expected);
  }

  @DataProvider
  public Object[][] nonIdenticalArraysWithNull() {
    return new Object[][] {
            { new String[] { "foo", "bar", null}, new String[] { "foo", "bar", "not-null"}},
            { new String[] { "foo", "not-null", "bar"}, new String[] { "foo", null, "bar"}},
            { new String[] { null, "foo", "bar"}, new String[] {" not-null", "foo", "bar"}}
    };
  }

  @Test(dataProvider="nonIdenticalArraysWithNull")
  public void nonIdenticalarrayWithNullValue(String[] actual, String[] expected) {
    Assert.assertNotEquals(actual, expected);
  }

}
