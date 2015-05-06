package test.lasttimeonly;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

public class LastTimeOnly {

    private int count = 1;

    public List<Integer> result = new LinkedList<Integer>();

    @BeforeMethod(firstTimeOnly = true)
    public void setUp() throws Exception {
        result.clear();
        result.add(0);
        assertSort();
    }

    @AfterMethod(lastTimeOnly = true)
    public void tearDown() throws Exception {
        result.add(Integer.MAX_VALUE);
        assertSort();
    }

    @Test(invocationCount = 10)
    public void testCount() throws Exception {
        result.add(count++);
        assertSort();
    }

    @DataProvider(name = "test1")
    public Object[][] createData1() {
        return new Object[][]{
                {"Cedric", 36},
                {"Anne", 37},
        };
    }

    @Test(invocationCount = 5, dataProvider = "test1")
    public void testDataProviderAndCount(String n1, Integer n2) {
        result.add(count++);
        assertSort();
    }

    @Test(dataProvider = "test1")
    public void testDataProvider(String n1, Integer n2) {
        result.add(count++);
        assertSort();
    }

    private void assertSort() {

        int last = Integer.MIN_VALUE;
        for (int i : result) {
            if (!(last < i)) throw new RuntimeException("List not sorted");
            last = i;
        }

    }
}
