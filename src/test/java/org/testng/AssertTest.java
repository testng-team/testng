package org.testng;

import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.internal.annotations.Sets;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


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
    Set expected = Sets.newHashSet();
    Set actual = Sets.newHashSet();

    expected.add(1);
    expected.add("a");
    actual.add("a");
    actual.add(1);

    Assert.assertEquals(actual, expected);
  }

  @Test
  public void mapAssertEquals() {
    Map expected = Maps.newHashMap();
    Map actual = Maps.newHashMap();

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
}
