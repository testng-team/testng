package org.testng;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import testhelper.PerformanceUtils;

public class AssertTest {
  @Test
  public void nullObjectArrayAssertEquals() {
    Object[] expected = null;
    Object[] actual = null;
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void nullObjectArrayAssertNoOrder() {
    Object[] expected = null;
    Object[] actual = null;
    Assert.assertEqualsNoOrder(actual, expected);
  }

  @Test
  public void nullCollectionAssertEquals() {
    Collection<?> expected = null;
    Collection<?> actual = null;
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void nullSetAssertEquals() {
    Set<?> expected = null;
    Set<?> actual = null;
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void testCollectionAssertEquals() {
    final Collection<Asymmetric> expected =
        Sets.newHashSet(new Asymmetric(10, 'a'), new Asymmetric(11, 'b'));
    final Collection<Asymmetric> actual =
        Sets.newHashSet(new Asymmetric(10, 'a'), new Asymmetric(11, 'b'));
    Assert.assertEquals(actual, expected);
  }

  @Test(description = "GITHUB-2483", expectedExceptions = AssertionError.class)
  public void testAsymetricNotEquals() {
    AsymetricEquals equalsSame = AsymetricEquals.equalsSame();
    AsymetricEquals equalsAll = AsymetricEquals.equalsAll();
    // sanity
    Assert.assertFalse(equalsSame.equals(equalsAll));
    Assert.assertTrue(equalsAll.equals(equalsSame));
    // actual check
    Assert.assertNotEquals(equalsSame, equalsAll);
  }

  @Test
  public void testListAssertNotEquals() {
    final Collection<Asymmetric> expected =
        Lists.newArrayList(new Asymmetric(10, 'a'), new Asymmetric(11, 'b'));
    final Collection<Asymmetric> actual =
        Lists.newArrayList(new Asymmetric(11, 'b'), new Asymmetric(10, 'a'));
    Assert.assertNotEquals(actual, expected);
  }

  @Test
  public void testSetAssertEquals() {
    final Set<Asymmetric> expected =
        Sets.newHashSet(new Asymmetric(10, 'a'), new Asymmetric(11, 'b'));
    final Set<Asymmetric> actual =
        Sets.newHashSet(new Asymmetric(10, 'a'), new Asymmetric(11, 'b'));
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void nullMapAssertEquals() {
    Map<?, ?> expected = null;
    Map<?, ?> actual = null;
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void setAssertEquals() {
    Set<Object> expected = Sets.newHashSet("a", 1);
    Set<Object> actual = Sets.newHashSet("a", 1);

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

  @Test(description = "GITHUB-2211")
  public void mapAssertEqualsWithMessage() {
    String expected = "User provided message";
    String actual = "";
    try {
      Assert.assertEquals(ImmutableMap.of("k", "v"), ImmutableMap.of(), expected);
    } catch (AssertionError error) {
      actual = error.getMessage();
    }
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void oneNullMapAssertEquals() {
    Map<?, ?> expected = Maps.newHashMap();
    Map<?, ?> actual = null;
    boolean failed = true;
    try {
      Assert.assertEquals(actual, expected);
      failed = false;
    } catch (AssertionError error) {
      // do nothing
    }
    Assert.assertTrue(failed, "assertEquals did not fail");
  }

  @Test
  public void oneNullSetAssertEquals() {
    Set<?> expected = null;
    Set<?> actual = Sets.newHashSet();
    boolean failed = true;
    try {
      Assert.assertEquals(actual, expected);
      failed = false;
    } catch (AssertionError error) {
      // do nothing
    }
    Assert.assertTrue(failed, "assertEquals did not fail");
  }

  /**
   * Testing comparison algorithm using big arrays.
   *
   * @see <a href="https://github.com/cbeust/testng/issues/1384">Issue #1384 – Huge performance
   *     issue between 6.5.2 and 6.11</a>
   */
  @Test
  public void compareLargeArrays() {
    if (!PerformanceUtils.canMeasureAllocatedMemory()) {
      throw new SkipException(
          "PerformanceUtils.canMeasureAllocatedMemory returns false, skipping test");
    }
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
    Assert.assertTrue(
        memoryUsage < 100 * 1024, "Amount of used memory should be approximately 65Kb");
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
    float[] actual = {
      (float) Math.PI,
      (float) Math.E,
      Float.MIN_VALUE,
      Float.MIN_NORMAL,
      Float.MAX_VALUE,
      Float.POSITIVE_INFINITY,
      Float.NEGATIVE_INFINITY
    };
    float[] expected = {
      (float) Math.PI,
      (float) Math.E,
      Float.MIN_VALUE,
      Float.MIN_NORMAL,
      Float.MAX_VALUE,
      Float.POSITIVE_INFINITY,
      Float.NEGATIVE_INFINITY
    };
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
    Assert.assertEquals(new float[] {Float.NaN}, new float[] {Float.NaN});
  }

  @Test
  public void compareFloatWithNaNValues() {
    Assert.assertEquals(Float.NaN, Float.NaN);
  }

  @Test
  public void compareDoubleArrays() {
    double[] actual = {
      Math.PI,
      Math.E,
      Double.MIN_VALUE,
      Double.MIN_NORMAL,
      Double.MAX_VALUE,
      Double.POSITIVE_INFINITY,
      Double.NEGATIVE_INFINITY
    };
    double[] expected = {
      Math.PI,
      Math.E,
      Double.MIN_VALUE,
      Double.MIN_NORMAL,
      Double.MAX_VALUE,
      Double.POSITIVE_INFINITY,
      Double.NEGATIVE_INFINITY
    };
    Assert.assertEquals(actual, expected);
  }

  @Test
  public void compareDoubleArraysWithNaNValues() {
    Assert.assertEquals(new double[] {Double.NaN}, new double[] {Double.NaN});
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

  @SuppressWarnings("serial")
  @Test(expectedExceptions = AssertionError.class)
  public void assertEqualsMapShouldFail() {
    Map<String, String> mapActual =
        new HashMap<String, String>() {
          {
            put("a", "1");
          }
        };
    Map<String, String> mapExpected =
        new HashMap<String, String>() {
          {
            put("a", "1");
            put("b", "2");
          }
        };

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

  @Test(expectedExceptions = AssertionError.class)
  public void testAssertEqualsWithActualBrokenEqualsTrue() {
    // BrokenEqualsTrue.equals(Object) always returns true, even for null
    // However, Assert is not supposed to check equals(Object) implementation, so assertEquals
    // should fail
    Assert.assertEquals(new BrokenEqualsTrue(), null);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testAssertEqualsWithExpectedBrokenEqualsTrue() {
    // BrokenEqualsTrue.equals(Object) always returns true, even for null
    // However, Assert is not supposed to check equals(Object) implementation, so assertEquals
    // should fail
    Assert.assertEquals(null, new BrokenEqualsTrue());
  }

  @Test
  public void testAssertEqualsWithNull() {
    Object obj = null;
    Assert.assertEquals(obj, obj);
  }

  @Test(
      description = "GITHUB-1935",
      expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp = "expected \\[y\\] but found \\[x\\]")
  public void testInequalityMessage() {
    Assert.assertEquals("x", "y");
  }

  @Test
  public void testAssertEqualsNoOrder() {
    String[] actual = {"a", "b"};
    String[] expected = {"b", "a"};
    Assert.assertEqualsNoOrder(actual, expected);
  }

  @Test
  public void testAssertEqualsNoOrderWithEmpty() {
    Assert.assertEqualsNoOrder(new String[0], new String[0]);
  }

  @Test
  public void testAssertEqualsNoOrderWithDifferentElementType() {
    Assert.assertEqualsNoOrder(new String[0], new Object[0]);
    Object[] actual = {"a"};
    String[] expected = {"a"};
    Assert.assertEqualsNoOrder(actual, expected);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testAssertEqualsNoOrderWithDuplicate() {
    String[] actual = {"a"};
    String[] expected = {"a", "a"};
    Assert.assertEqualsNoOrder(actual, expected);
  }

  @Test
  public void testAssertEqualsNoOrderWithBothNull() {
    Assert.assertEqualsNoOrder((Object[]) null, (Object[]) null);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testAssertEqualsNoOrderWithActualNull() {
    Assert.assertEqualsNoOrder(null, new String[0]);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testAssertEqualsNoOrderWithExpectedNull() {
    Assert.assertEqualsNoOrder(new String[0], null);
  }

  @Test(description = "GITHUB-2500", expectedExceptions = AssertionError.class)
  public void testAssertEqualsNoOrderNotDeep() {
    // assertEqualsNoOrder does not compare arrays deeply, so elements which are
    // arrays as well are only compared for reference equality
    String[][] actual = {{}};
    String[][] expected = {{}};
    Assert.assertEqualsNoOrder(actual, expected);
  }

  @Test(
      description = "GITHUB-2080",
      expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp = "test expected \\[true\\] but found \\[false\\]")
  public void testAssertTrueMessage() {
    Assert.assertTrue(false, "test");
  }

  @Test(
      description = "GITHUB-2080",
      expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp = "test expected \\[false\\] but found \\[true\\]")
  public void testAssertFalseMessage() {
    Assert.assertFalse(true, "test");
  }

  @Test(
      description = "GITHUB-2080",
      expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp = "did not expect \\[x\\] but found \\[x\\]")
  public void testEqualityMessage() {
    Assert.assertNotEquals("x", "x");
  }

  @Test(
      description = "GITHUB-2118",
      expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp = "did not expect \\[1.5\\] but found \\[1.5\\]")
  public void testAssertNotEqualsFloat() {
    Assert.assertNotEquals(1.5f, 1.5f, 0.5f);
  }

  @Test(
      description = "GITHUB-2118",
      expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp = "did not expect \\[1.5\\] but found \\[1.5\\]")
  public void testAssertNotEqualsDouble() {
    Assert.assertNotEquals(1.5, 1.5, 0.5);
  }

  @Test(
      description = "GITHUB-2118",
      expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp =
          "did not expect \\[\\[test\\]\\] but found \\[\\[test\\]\\]")
  public void testAssertNotEqualsSet() {
    Set<String> set = new HashSet<>();
    set.add("test");
    Assert.assertNotEquals(set, set);
  }

  @Test(
      description = "GITHUB-2118",
      expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp =
          "did not expect \\[\\[test\\]\\] but found \\[\\[test\\]\\]")
  public void testAssertNotEqualsDeepSet() {
    Set<String> set = new HashSet<>();
    set.add("test");
    Assert.assertNotEqualsDeep(set, set);
  }

  @Test(
      description = "GITHUB-2118",
      expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp =
          "did not expect \\[\\{key=value\\}\\] but found \\[\\{key=value\\}\\]")
  public void testAssertNotEqualsMap() {
    Map<String, String> map = new HashMap<>();
    map.put("key", "value");
    Assert.assertNotEquals(map, map);
  }

  @Test(
      description = "GITHUB-2118",
      expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp =
          "did not expect \\[\\{key=value\\}\\] but found \\[\\{key=value\\}\\]")
  public void testAssertNotEqualsDeepMap() {
    Map<String, String> map = new HashMap<>();
    map.put("key", "value");
    Assert.assertNotEqualsDeep(map, map);
  }

  @Test(description = "GITHUB-2486")
  public void testAssertNotSame() {
    Contrived object = new Contrived(2);
    Contrived object2 = new Contrived(2);
    Assert.assertNotSame(object2, object);
  }

  @Test(description = "GITHUB-2486", expectedExceptions = AssertionError.class)
  public void testAssertSame() {
    Contrived object = new Contrived(2);
    Contrived object2 = new Contrived(2);
    Assert.assertSame(object2, object);
  }

  @Test(description = "GITHUB-2490")
  public void testAssertNotEqualsWithActualBrokenEqualsTrue() {
    // BrokenEqualsTrue.equals(Object) always returns true, even for null
    // However, Assert is not supposed to check equals(Object) implementation, so assertNotEquals
    // should succeed
    Assert.assertNotEquals(new BrokenEqualsTrue(), null);
  }

  @Test(description = "GITHUB-2490")
  public void testAssertNotEqualsWithExpectedBrokenEqualsTrue() {
    // BrokenEqualsTrue.equals(Object) always returns true, even for null
    // However, Assert is not supposed to check equals(Object) implementation, so assertNotEquals
    // should succeed
    Assert.assertNotEquals(null, new BrokenEqualsTrue());
  }

  @Test(description = "GITHUB-2490")
  public void testAssertNotEqualsWithActualBrokenEqualsFalse() {
    // BrokenEqualsFalse.equals(Object) always returns false; assertNotEquals should fail
    Assert.assertNotEquals(new BrokenEqualsFalse(), null);
  }

  @Test(description = "GITHUB-2490")
  public void testAssertNotEqualsWithExpectedBrokenEqualsFalse() {
    // BrokenEqualsFalse.equals(Object) always returns false; assertNotEquals should fail
    Assert.assertNotEquals(null, new BrokenEqualsFalse());
  }

  @Test(description = "GITHUB-2490")
  public void testAssertNotEqualsWithExpectedNullAndNewObject() {
    Assert.assertNotEquals(new Object(), null);
  }

  @Test
  public void testAssertNotEqualsWithActualNullExceptedEmptyString() {
    Assert.assertNotEquals(null, "");
  }

  @Test
  public void testAssertNotEqualsWithActualEmptyStringExceptedNull() {
    Assert.assertNotEquals("", null);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testAssertNotEqualsWithNull() {
    Object obj = null;
    Assert.assertNotEquals(obj, obj);
  }

  class Contrived {

    int integer;

    Contrived(int integer) {
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

  static class AsymetricEquals {

    static AsymetricEquals equalsAll() {
      return new AsymetricEquals(null);
    }

    static AsymetricEquals equalsSame() {
      return new AsymetricEquals(new Object());
    }

    Object value;

    private AsymetricEquals(Object value) {
      this.value = value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof AsymetricEquals)) return false;

      AsymetricEquals that = (AsymetricEquals) o;
      if (value == null) return true;
      return value.equals(that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(value);
    }
  }

  static class BrokenEqualsTrue {
    @Override
    public boolean equals(Object o) {
      return true; // broken implementation
    }
  }

  static class BrokenEqualsFalse {
    @Override
    public boolean equals(Object o) {
      return false; // broken implementation
    }
  }
}
