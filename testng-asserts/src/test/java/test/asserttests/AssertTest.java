package test.asserttests;

import static org.testng.Assert.*;

import java.io.IOException;
import java.util.*;
import org.testng.Assert;
import org.testng.Assert.ThrowingRunnable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

public class AssertTest {

  @Test(description = "GITHUB-2652")
  public void assertEqualsBoxedUnboxedDouble() {
    Double a = 3.14;
    double b = 3.14;
    Double deltaA = 0.1;
    double deltaB = 0.1;
    assertEquals(a, b, deltaA);
    assertEquals(a, b, deltaB);
    assertEquals(a, b);
    assertEquals(a, b, "");
    assertEquals(b, a);
    assertEquals(b, a, "");
  }

  @Test(description = "GITHUB-2652")
  public void assertEqualsBoxedUnboxedFloat() {
    Float a = 3.1f;
    float b = 3.1f;
    Float deltaA = 0.1f;
    float deltaB = 0.1f;
    assertEquals(a, b, deltaA);
    assertEquals(a, b, deltaB);
    assertEquals(a, b);
    assertEquals(a, b, "");
    assertEquals(b, a);
    assertEquals(b, a, "");
  }

  @Test(description = "GITHUB-2652")
  public void assertEqualsBoxedUnboxedLong() {
    Long a = 3L;
    long b = 3L;
    Long deltaA = 1L;
    long deltaB = 1L;
    assertEquals(a, b, deltaA);
    assertEquals(a, b, deltaB);
    assertEquals(a, b);
    assertEquals(a, b, "");
    assertEquals(b, a);
    assertEquals(b, a, "");
  }

  @Test(description = "GITHUB-2652")
  public void assertEqualsBoxedUnboxedBoolean() {
    Boolean a = true;
    boolean b = true;
    assertEquals(a, b);
    assertEquals(a, b, "");
    assertEquals(b, a);
    assertEquals(b, a, "");
  }

  @Test(description = "GITHUB-2652")
  public void assertEqualsBoxedUnboxedByte() {
    Byte a = Byte.valueOf("3");
    byte b = (byte) 3;
    assertEquals(a, b);
    assertEquals(a, b, "");
    assertEquals(b, a);
    assertEquals(b, a, "");
  }

  @Test(description = "GITHUB-2652")
  public void assertEqualsBoxedUnboxedChar() {
    Character a = 'a';
    char b = 'a';
    assertEquals(a, b);
    assertEquals(a, b, "");
    assertEquals(b, a);
    assertEquals(b, a, "");
  }

  @Test(description = "GITHUB-2652")
  public void assertEqualsBoxedUnboxedShort() {
    Short a = Short.valueOf("3");
    short b = (short) 3;
    Short deltaA = Short.valueOf("3");
    short deltaB = (short) 1;
    assertEquals(a, b, deltaA);
    assertEquals(a, b, deltaB);
    assertEquals(a, b);
    assertEquals(a, b, "");
    assertEquals(b, a);
    assertEquals(b, a, "");
  }

  @Test(description = "GITHUB-2652")
  public void assertEqualsBoxedUnboxedInteger() {
    Integer a = 3;
    int b = 3;
    Integer deltaA = 1;
    int deltaB = 1;
    assertEquals(a, b, deltaA);
    assertEquals(a, b, deltaB);
    assertEquals(a, b);
    assertEquals(a, b, "");
    assertEquals(b, a);
    assertEquals(b, a, "");
  }

  @Test
  public void noOrderSuccess() {
    String[] rto1 = {
      "boolean", "BigInteger", "List",
    };
    String[] rto2 = {
      "List", "BigInteger", "boolean",
    };
    Assert.assertEqualsNoOrder(rto1, rto2);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void noOrderFailure() {
    String[] rto1 = {
      "a", "a", "b",
    };
    String[] rto2 = {
      "a", "b", "b",
    };
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
      assertEquals(
          "Expected IOException to be thrown, but NullPointerException was thrown",
          error.getMessage());
      assertSame(npe, error.getCause());
      return;
    }
    fail();
  }

  private static ThrowingRunnable nonThrowingRunnable() {
    return () -> {};
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
    return new Object[][] {
      {new String[] {"foo", "bar", null}, new String[] {"foo", "bar", null}},
      {new String[] {"foo", null, "bar"}, new String[] {"foo", null, "bar"}},
      {new String[] {null, "foo", "bar"}, new String[] {null, "foo", "bar"}}
    };
  }

  @Test(dataProvider = "identicalArraysWithNull")
  public void identicalArraysWithNullValues(String[] actual, String[] expected) {
    assertEquals(actual, expected);
  }

  @DataProvider
  public Object[][] nonIdenticalArraysWithNull() {
    return new Object[][] {
      {new String[] {"foo", "bar", null}, new String[] {"foo", "bar", "not-null"}},
      {new String[] {"foo", "not-null", "bar"}, new String[] {"foo", null, "bar"}},
      {new String[] {null, "foo", "bar"}, new String[] {" not-null", "foo", "bar"}}
    };
  }

  @Test(dataProvider = "nonIdenticalArraysWithNull")
  public void nonIdenticalarrayWithNullValue(String[] actual, String[] expected) {
    Assert.assertNotEquals(actual, expected);
  }

  @Test(description = "GITHUB-2540", expectedExceptions = AssertionError.class)
  public void checkCollectionEqualsFailsWhenDifferentOrder() {

    Collection<String> collection1 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
    Collection<String> collection2 = new LinkedHashSet<>(Arrays.asList("a", "c", "b"));

    assertEquals(collection1, collection2);
  }

  @Test(description = "GITHUB-2540")
  public void checkCollectionEqualsNoOrder() {

    Collection<String> collection1 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
    Collection<String> collection2 = new LinkedHashSet<>(Arrays.asList("a", "c", "b"));

    assertEqualsNoOrder(collection1, collection2);
  }

  @Test(description = "GITHUB-2540")
  public void checkCollectionEquals() {

    Collection<String> collection1 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
    Collection<String> collection2 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));

    assertEquals(collection1, collection2);
  }

  @Test(description = "GITHUB-2540")
  public void checkCollectionNotEquals() {

    Collection<String> collection1 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
    Collection<String> collection2 = new LinkedHashSet<>(Arrays.asList("a", "c", "b"));

    assertNotEquals(collection1, collection2);
  }

  @Test(description = "GITHUB-2540", expectedExceptions = AssertionError.class)
  public void checkCollectionNotEqualsFailsWhenDifferentOrder() {

    Collection<String> collection1 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
    Collection<String> collection2 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));

    assertNotEquals(collection1, collection2);
  }

  @Test(description = "GITHUB-2540", expectedExceptions = AssertionError.class)
  public void checkIteratorEqualsFailsWhenDifferentOrder() {

    Iterator<String> iterator1 = (new LinkedHashSet<>(Arrays.asList("a", "b", "c"))).iterator();
    Iterator<String> iterator2 = (new LinkedHashSet<>(Arrays.asList("a", "c", "b"))).iterator();

    assertEquals(iterator1, iterator2);
  }

  @Test(description = "GITHUB-2540")
  public void checkIteratorEqualsNoOrder() {

    Iterator<String> iterator1 = (new LinkedHashSet<>(Arrays.asList("a", "b", "c"))).iterator();
    Iterator<String> iterator2 = (new LinkedHashSet<>(Arrays.asList("a", "c", "b"))).iterator();

    assertEqualsNoOrder(iterator1, iterator2);
  }

  @Test(description = "GITHUB-2540")
  public void checkIteratorEquals() {

    Iterator<String> iterator1 = (new LinkedHashSet<>(Arrays.asList("a", "b", "c"))).iterator();
    Iterator<String> iterator2 = (new LinkedHashSet<>(Arrays.asList("a", "b", "c"))).iterator();

    assertEquals(iterator1, iterator2);
  }

  @Test(description = "GITHUB-2540")
  public void checkIteratorNotEquals() {

    Iterator<String> iterator1 = (new LinkedHashSet<>(Arrays.asList("a", "b", "c"))).iterator();
    Iterator<String> iterator2 = (new LinkedHashSet<>(Arrays.asList("a", "c", "b"))).iterator();

    assertNotEquals(iterator1, iterator2);
  }

  @Test(description = "GITHUB-2540", expectedExceptions = AssertionError.class)
  public void checkIteratorNotEqualsFailsWhenDifferentOrder() {

    Iterator<String> iterator1 = (new LinkedHashSet<>(Arrays.asList("a", "b", "c"))).iterator();
    Iterator<String> iterator2 = (new LinkedHashSet<>(Arrays.asList("a", "b", "c"))).iterator();

    assertNotEquals(iterator1, iterator2);
  }

  @Test(description = "GITHUB-2643")
  public void checkSetEqualsWhenDifferentOrder() {

    Set<String> set1 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
    Set<String> set2 = new LinkedHashSet<>(Arrays.asList("a", "c", "b"));

    assertEquals(set1, set2);
  }

  @Test(description = "GITHUB-2540")
  public void checkSetEqualsNoOrder() {

    Set<String> set1 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
    Set<String> set2 = new LinkedHashSet<>(Arrays.asList("a", "c", "b"));

    assertEqualsNoOrder(set1, set2);
  }

  @Test(description = "GITHUB-2540")
  public void checkSetEquals() {

    Set<String> set1 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
    Set<String> set2 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));

    assertEquals(set1, set2);
  }

  @Test(description = "GITHUB-2643", expectedExceptions = AssertionError.class)
  public void checkSetNotEqualsFailsWhenDifferentOrder() {

    Set<String> set1 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
    Set<String> set2 = new LinkedHashSet<>(Arrays.asList("a", "c", "b"));

    assertNotEquals(set1, set2);
  }

  @Test(description = "GITHUB-2643", expectedExceptions = AssertionError.class)
  public void checkSetNotEqualsFailsWhenSameOrder() {

    Set<String> set1 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
    Set<String> set2 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));

    assertNotEquals(set1, set2);
  }

  @Test(description = "GITHUB-2643")
  public void checkSetNotEqualsWhenNotSameSets() {

    Set<String> set1 = new LinkedHashSet<>(Arrays.asList("a", "b"));
    Set<String> set2 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));

    assertNotEquals(set1, set2);
  }

  @Test(description = "GITHUB-2643", expectedExceptions = AssertionError.class)
  public void checkSetEqualsFailsWhenNotSameSets() {

    Set<String> set1 = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
    Set<String> set2 = new LinkedHashSet<>(Arrays.asList("a", "b"));

    assertEquals(set1, set2);
  }

  @Test(description = "GITHUB-2540", expectedExceptions = AssertionError.class)
  public void checkArrayEqualsFailsWhenDifferentOrder() {

    String[] array1 = {"a", "b", "c"};
    String[] array2 = {"a", "c", "b"};

    assertEquals(array1, array2);
  }

  @Test(description = "GITHUB-2540")
  public void checkArrayEqualsNoOrder() {

    String[] array1 = {"a", "b", "c"};
    String[] array2 = {"a", "c", "b"};

    assertEqualsNoOrder(array1, array2);
  }

  @Test(description = "GITHUB-2540")
  public void checkArrayEquals() {

    String[] array1 = {"a", "b", "c"};
    String[] array2 = {"a", "b", "c"};

    assertEquals(array1, array2);
  }

  @Test(description = "GITHUB-2540")
  public void checkArrayNotEquals() {

    String[] array1 = {"a", "b", "c"};
    String[] array2 = {"a", "c", "b"};

    assertNotEquals(array1, array2);
  }

  @Test(description = "GITHUB-2540", expectedExceptions = AssertionError.class)
  public void checkArrayNotEqualsFailsWhenDifferentOrder() {

    String[] array1 = {"a", "b", "c"};
    String[] array2 = {"a", "b", "c"};

    assertNotEquals(array1, array2);
  }
}
