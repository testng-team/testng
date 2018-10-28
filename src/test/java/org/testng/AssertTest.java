package org.testng;

import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import testhelper.PerformanceUtils;

import java.util.*;


/**
 * This class/interface
 */
public class AssertTest {
  @Test
  public void nullObjectArrayAssertEquals() {
    Object[] expected= null;
    Object[] actual= null;
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void nullObjectArrayAssertNoOrder() {
    Object[] expected= null;
    Object[] actual= null;
    Assert.assertEqualsNoOrder(actual, expected);
  }

  @Test
  public void nullCollectionAssertEquals() {
    Collection expected = null;
    Collection actual = null;
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void nullSetAssertEquals() {
    Set expected = null;
    Set actual = null;
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void nullMapAssertEquals() {
    Map expected = null;
    Map actual = null;
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void setAssertEquals() {
    Set<Object> expected = Sets.newHashSet();
    Set<Object> actual = Sets.newHashSet();

    expected.add(1);
    expected.add("a");
    actual.add("a");
    actual.add(1);

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void mapAssertEquals() {
    Map<Object, Object> expected = Maps.newHashMap();
    Map<Object, Object> actual = Maps.newHashMap();

    expected.put(null, "a");
    expected.put("a", "a");
    expected.put("b", "c");
    actual.put("b", "c");
    actual.put(null, "a");
    actual.put("a", "a");

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void oneNullMapAssertEquals() {
    Map expected = Maps.newHashMap();
    Map actual = null;
    try {
      Assert.assertEquals(actual, expected);
      Assert.fail("AssertEquals didn't fail");
    }
    catch (AssertionError error) {
      //do nothing
    }
  }

  @Test
  public void oneNullSetAssertEquals() {
    Set expected = null;
    Set actual = Sets.newHashSet();
    try {
      Assert.assertEquals(actual, expected);
      Assert.fail("AssertEquals didn't fail");
    }
    catch (AssertionError error) {
      //do nothing
    }
  }

  /**
   * Testing comparison algorithm using big arrays.
   *
   * @see <a href="https://github.com/cbeust/testng/issues/1384">Issue #1384 – Huge performance issue between 6.5.2
   * and 6.11</a>
   */
  @Test
  public void compareLargeArrays() {
    int length = 1024 * 100;
    byte[] first = new byte[length];
    byte[] second = new byte[length];

    Random rnd = new Random();
    rnd.nextBytes(first);
    System.arraycopy(first, 0, second, 0, length);

    long before = PerformanceUtils.measureAllocatedMemory();
    Assert.assertEquals(first, second);
    long memoryUsage = PerformanceUtils.measureAllocatedMemory() - before;

    // assertEquals() with primitive type arrays requires ~65Kb of memory
    // assertEquals() with Object-type requires ~3Mb of memory when comparing 100Kb arrays.
    // choosing 100Kb as a threshold
    Assert.assertTrue(memoryUsage < 100 * 1024, "Amount of used memory should be approximately 65Kb");
  }

  @Test
  public void compareShortArrays() {
    short[] actual = {Short.MIN_VALUE, 0, Short.MAX_VALUE};
    short[] expected = {Short.MIN_VALUE, 0, Short.MAX_VALUE};
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void compareIntArrays() {
    int[] actual = {Integer.MIN_VALUE, 0, Integer.MAX_VALUE};
    int[] expected = {Integer.MIN_VALUE, 0, Integer.MAX_VALUE};
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void compareLongArrays() {
    long[] actual = {Long.MIN_VALUE, 0, Long.MAX_VALUE};
    long[] expected = {Long.MIN_VALUE, 0, Long.MAX_VALUE};
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void compareBooleanArrays() {
    boolean[] actual = {true, false};
    boolean[] expected = {true, false};
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void compareCharacterArrays() {
    char[] actual = {'a', '1', '#'};
    char[] expected = {'a', '1', '#'};
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void compareFloatArrays() {
    float[] actual = {(float) Math.PI, (float) Math.E, Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE,
            Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
    float[] expected = {(float) Math.PI, (float) Math.E, Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE,
            Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void compareFloatArraysWithDelta() {
    float[] actual = {0.1f, 0.2f, 0.3f, 0.4f};
    float[] expected = {0.5f, 0.7f, 0.1f, 0.2f};
    Assert.assertEquals(actual, expected, 0.5f);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void compareUnEqualFloatArraysWithDelta() {
    float[] actual = {0.1f, 0.2f, 0.3f, 0.4f};
    float[] expected = {0.5f, 0.7f, 0.1f, 0.2f};
    Assert.assertEquals(actual, expected, 0.1f);
  }

  @Test
  public void compareFloatArraysWithNaNValues() {
    Assert.assertEquals(new float[] { Float.NaN }, new float[] { Float.NaN });
  }

  @Test
  public void compareFloatWithNaNValues() {
    Assert.assertEquals(Float.NaN, Float.NaN);
  }

  @Test
  public void compareDoubleArrays() {
    double[] actual = {Math.PI, Math.E, Double.MIN_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE,
            Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
    double[] expected = {Math.PI, Math.E, Double.MIN_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE,
            Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void compareDoubleArraysWithNaNValues() {
    Assert.assertEquals(new double[] { Double.NaN }, new double[] { Double.NaN });
  }

  @Test
  public void compareDoubleWithNaNValues() {
    Assert.assertEquals(Double.NaN, Double.NaN);
  }

  @Test
  public void compareDoubleArraysWithDelta() {
    double[] actual = {0.1d, 0.2d, 0.3d, 0.4d};
    double[] expected = {0.5d, 0.7d, 0.1d, 0.2d};
    Assert.assertEquals(actual, expected, 0.5d);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void compareUnEqualDoubleArraysWithDelta() {
    double[] actual = {0.1d, 0.2d, 0.3d, 0.4d};
    double[] expected = {0.5d, 0.7d, 0.1d, 0.2d};
    Assert.assertEquals(actual, expected, 0.1d);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void assertEqualsMapShouldFail() {
    Map<String, String> mapActual = new HashMap<String, String>() {{
      put("a","1");
    }};
    Map<String, String> mapExpected = new HashMap<String, String>() {{
      put("a","1");
      put("b","2");
    }};

    Assert.assertEquals(mapActual, mapExpected);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void assertEqualsSymmetricScalar() {
    Assert.assertEquals(new Asymmetric(42, 'd'), new Contrived(42));
  }

  @Test(expectedExceptions = AssertionError.class)
  public void assertEqualsSymmetricArrays() {
    Object[] actual = {1, new Asymmetric(42, 'd'), "inDay"};
    Object[] expected = {1, new Contrived(42), "inDay"};
    Assert.assertEquals(actual, expected);
  }

  @Test(description = "GITHUB-1935", expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp = "expected \\[y\\] but found \\[x\\]")
  public void testInequalityMessage() {
    Assert.assertEquals("x", "y");
  }

  @Test(description = "GITHUB-1935", expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp = "did not expect to find \\[x\\] but found \\[x\\]")
  public void testEqualityMessage() {
    Assert.assertNotEquals("x", "x");
  }

  class Contrived {

    int integer;

    Contrived(int integer){
      this.integer = integer;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Contrived)) return false;

      Contrived contrived = (Contrived) o;

      return integer == contrived.integer;
    }

    @Override
    public int hashCode() {
      return integer;
    }
  }

  class Asymmetric extends Contrived {

      char character;

      Asymmetric(int integer, char character) {
          super(integer);
          this.character = character;
      }

      @Override
      public boolean equals(Object o) {
          if (this == o) return true;
          if (!(o instanceof Asymmetric)) return false;
          if (!super.equals(o)) return false;

          Asymmetric that = (Asymmetric) o;

        return character == that.character;
      }

      @Override
      public int hashCode() {
          int result = super.hashCode();
          result = 31 * result + (int) character;
          return result;
      }
  }
}
