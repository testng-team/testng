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
    public void arrayAssertEquals() {
        assertEquals(new int[]{ 42 }, new int[] { 42 },
                "arrays of primitives are compared by value in assertEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayAssertNotEquals() {
        assertNotEquals(new int[]{ 42 }, new int[] { 42 },
                "arrays of primitives are compared by value in assertNotEquals");
    }

    @Test
    public void boxedArrayAssertEquals() {
        assertEquals(new Integer[]{ 42 }, new Integer[] { 42 },
                "arrays of wrapped values are compared by value in assertEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void boxedArrayAssertNotEquals() {
        assertNotEquals(new Integer[]{ 42 }, new Integer[] { 42 },
                "arrays of wrapped values are compared by value in assertNotEquals");
    }

    @Test
    public void mixedArraysAssertEquals() {
        assertEquals(new int[]{ 42 }, new Integer[] { 42 },
                "arrays of wrapped values are compared by value in assertEquals");
        assertEquals(new Integer[]{ 42 }, new int[] { 42 },
                "arrays of wrapped values are compared by value in assertEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void mixedArraysAssertNotEquals() {
        assertNotEquals(new int[]{ 42 }, new Integer[] { 42 },
                "arrays of wrapped values are compared by value in assertNotEquals");
        assertNotEquals(new Integer[]{ 42 }, new int[] { 42 },
                "arrays of wrapped values are compared by value in assertNotEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayInsideListAssertEquals() {
        List<int[]> list = Arrays.asList(
            new int[]{ 42 }
        );
        List<int[]> listCopy = Arrays.asList(
            new int[]{ 42 }
        );
        assertEquals(list, listCopy,
                "arrays inside lists are compared by reference in assertEquals");
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
                "arrays inside lists are compared by reference in assertNotEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayInsideMapAssertEquals() {
        Map<String, int[]> map = new HashMap<>();
        map.put("array", new int[]{ 42 });
        Map<String, int[]> mapCopy = new HashMap<>();
        mapCopy.put("array", new int[]{ 42 });

        // arrays inside maps are compared by reference in assertEquals(Map,Map)
        assertEquals(map, mapCopy);
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayInsideMapAssertEqualsWithMessage() {
        Map<String, int[]> map = new HashMap<>();
        map.put("array", new int[]{ 42 });
        Map<String, int[]> mapCopy = new HashMap<>();
        mapCopy.put("array", new int[]{ 42 });

        assertEquals(map, mapCopy,
                "arrays inside maps are compared by reference in assertEquals(Map,Map,String)");
    }

    @Test
    public void arrayInsideMapAssertNotEquals() {
        Map<String, int[]> map = new HashMap<>();
        map.put("array", new int[]{ 42 });
        Map<String, int[]> mapCopy = new HashMap<>();
        mapCopy.put("array", new int[]{ 42 });

        assertNotEquals(map, mapCopy,
                "arrays inside maps are compared by reference in assertNotEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayInsideSetAssertEquals() {
        Set<int[]> set = new HashSet<>();
        set.add(new int[]{ 42 });
        Set<int[]> setCopy = new HashSet<>();
        setCopy.add(new int[]{ 42 });

        assertEquals(set, setCopy,
                "arrays inside sets are compared by reference in assertNotEquals");
    }

    @Test
    public void arrayInsideSetAssertNotEquals() {
        Set<int[]> set = new HashSet<>();
        set.add(new int[]{ 42 });
        Set<int[]> setCopy = new HashSet<>();
        setCopy.add(new int[]{ 42 });

        assertNotEquals(set, setCopy,
                "arrays inside sets are compared by reference in assertNotEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayDeepInListsAssertEquals() {
        List<List<int[]>> list = Collections.singletonList(Arrays.asList(new int[]{ 42 }));
        List<List<int[]>> listCopy = Collections.singletonList(Arrays.asList(new int[]{ 42 }));

        assertEquals(list, listCopy,
                "arrays inside lists which are inside lists themselves are compared by reference in assertEquals");
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
                "arrays inside maps which are inside maps themselves are compared by reference in assertEquals");
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
                "arrays inside maps which are inside lists themselves are compared by reference in assertEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayDeepInMapAndListAssertEquals() {
        Map<String, List<int[]>> map = new HashMap<>();
        map.put("list", Arrays.asList(new int[]{ 42 }));
        Map<String, List<int[]>> mapCopy = new HashMap<>();
        mapCopy.put("list", Arrays.asList(new int[]{ 42 }));

        assertEquals(map, mapCopy,
                "arrays inside maps which are inside lists themselves are compared by reference in assertEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayInsideIterableAssertEquals() {
        Iterable<int[]> iterable = Arrays.asList(
            new int[]{ 42 }
        );
        Iterable<int[]> iterableCopy = Arrays.asList(
            new int[]{ 42 }
        );
        assertEquals(iterable, iterableCopy,
                "arrays inside Iterables are compared by reference in assertEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayDeepInIterablesAssertEquals() {
        List<List<int[]>> iterable = Collections.singletonList(Arrays.asList(new int[]{ 42 }));
        List<List<int[]>> iterableCopy = Collections.singletonList(Arrays.asList(new int[]{ 42 }));

        assertEquals(iterable, iterableCopy,
                "arrays inside Iterables which are inside Iterables themselves are compared by reference in assertEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayInsideArrayAssertEquals() {
        int[][] array = new int[][] {
            new int[]{ 42 }
        };
        int[][] arrayCopy = new int[][] {
            new int[]{ 42 }
        };
        assertEquals(array, arrayCopy,
                "arrays inside arrays are compared by reference in assertEquals");
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayDeepInArraysAssertEquals() {
        int[][][] array = new int[][][] {
            new int[][] { new int[]{ 42 } }
        };
        int[][][] arrayCopy = new int[][][] {
            new int[][] { new int[]{ 42 } }
        };

        assertEquals(array, arrayCopy,
                "arrays inside arrays which are inside arrays themselves are compared by reference in assertEquals");
    }

    @SuppressWarnings("unchecked")
    @Test(expectedExceptions = AssertionError.class)
    public void arrayDeepInArrayAndListAssertEquals() {
        List<int[]>[] array = new List[] {
            Arrays.asList(new int[]{ 42 })
        };
        List<int[]>[] arrayCopy = new List[] {
            Arrays.asList(new int[]{ 42 })
        };

        assertEquals(array, arrayCopy,
                "arrays inside arrays which are inside arrays themselves are compared by reference in assertEquals");
    }

}
