package test;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class MapTest {
  @Test(description = "Make sure that arrays as map values work with assert")
  public void testMap() {
    Map<String, boolean[]> mapA = new HashMap<String, boolean[]>();
    Map<String, boolean[]> mapB = new HashMap<String, boolean[]>();
    mapA.put("A", new boolean[] { true });
    mapB.put("A", new boolean[] { true });
    Assert.assertEquals(mapA, mapB);
  }
}