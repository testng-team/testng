package test.asserttests;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * Tests different equality cases for nested collections
 * and arrays.
 */
public class ArrayEqualityAssertTest {
    @Test
    public void arrayInsideListAssertEquals() {
        List<int[]> list = Arrays.asList(
            new int[]{ 42 }
        );
        List<int[]> listCopy = Arrays.asList(
            new int[]{ 42 }
        );
        assertEquals(list, listCopy,
                "arrays inside lists are compared by value in assertEquals");
    }

    @Test
    public void arrayInsideListAssertNotEquals() {
        List<int[]> list = Arrays.asList(
            new int[]{ 42 }
        );
        List<int[]> listCopy = Arrays.asList(
            new int[]{ 42 }
        );
        assertNotEquals(list, listCopy,
                "arrays inside lists aren't compared by value in assertNotEquals");
    }

    @Test
    public void arrayInsideMapAssertEquals() {
        Map<String, int[]> map = new HashMap<>();
        map.put("array", new int[]{ 42 });
        Map<String, int[]> mapCopy = new HashMap<>();
        mapCopy.put("array", new int[]{ 42 });

        // arrays inside maps are compared by value in assertEquals(Map,Map)
        assertEquals(map, mapCopy);
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayInsideMapAssertEqualsWithMessage() {
        Map<String, int[]> map = new HashMap<>();
        map.put("array", new int[]{ 42 });
        Map<String, int[]> mapCopy = new HashMap<>();
        mapCopy.put("array", new int[]{ 42 });

        assertEquals(map, mapCopy,
                "arrays inside maps aren't compared by value in assertEquals(Map,Map,String)");
    }

    @Test
    public void arrayInsideMapAssertNotEquals() {
        Map<String, int[]> map = new HashMap<>();
        map.put("array", new int[]{ 42 });
        Map<String, int[]> mapCopy = new HashMap<>();
        mapCopy.put("array", new int[]{ 42 });

        assertNotEquals(map, mapCopy,
                "arrays inside maps aren't compared by value in assertNotEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayInsideSetAssertEquals() {
        Set<int[]> set = new HashSet<>();
        set.add(new int[]{ 42 });
        Set<int[]> setCopy = new HashSet<>();
        setCopy.add(new int[]{ 42 });

        assertEquals(set, setCopy,
                "arrays inside sets aren't compared by value in assertNotEquals");
    }

    @Test
    public void arrayInsideSetAssertNotEquals() {
        Set<int[]> set = new HashSet<>();
        set.add(new int[]{ 42 });
        Set<int[]> setCopy = new HashSet<>();
        setCopy.add(new int[]{ 42 });

        assertNotEquals(set, setCopy,
                "arrays inside sets aren't compared by value in assertNotEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayDeepInListsAssertEquals() {
        List<List<int[]>> list = Collections.singletonList(Arrays.asList(new int[]{ 42 }));
        List<List<int[]>> listCopy = Collections.singletonList(Arrays.asList(new int[]{ 42 }));

        assertEquals(list, listCopy,
                "arrays inside lists which are inside lists themselves aren't compared by value in assertEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayDeepInMapsAssertEquals() {
        Map<String, Map<String, int[]>> map = new HashMap<>();
        Map<String, int[]> innerMap = new HashMap<>();
        innerMap.put("array", new int[]{ 42 });
        map.put("map", innerMap);
        Map<String, Map<String, int[]>> mapCopy = new HashMap<>();
        Map<String, int[]> innerMapCopy = new HashMap<>();
        innerMapCopy.put("array", new int[]{ 42 });
        mapCopy.put("map", innerMapCopy);

        assertEquals(map, mapCopy,
                "arrays inside maps which are inside maps themselves aren't compared by value in assertEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayDeepInListAndMapAssertEquals() {
        List<Map<String, int[]>> list = new ArrayList<>();
        Map<String, int[]> innerMap = new HashMap<>();
        innerMap.put("array", new int[]{ 42 });
        list.add(innerMap);
        List<Map<String, int[]>> listCopy = new ArrayList<>();
        Map<String, int[]> innerMapCopy = new HashMap<>();
        innerMapCopy.put("array", new int[]{ 42 });
        list.add(innerMapCopy);

        assertEquals(list, listCopy,
                "arrays inside maps which are inside lists themselves aren't compared by value in assertEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayDeepInMapAndListAssertEquals() {
        Map<String, List<int[]>> map = new HashMap<>();
        map.put("list", Arrays.asList(new int[]{ 42 }));
        Map<String, List<int[]>> mapCopy = new HashMap<>();
        mapCopy.put("list", Arrays.asList(new int[]{ 42 }));

        assertEquals(map, mapCopy,
                "arrays inside maps which are inside lists themselves aren't compared by value in assertEquals");
    }

    // Test Iterable and assertArrayEquals()
}
