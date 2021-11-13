package test.reports;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GitHub447Sample {

  @Test(dataProvider = "add")
  public void add(List<Object> list, Object e, String expected) {
    assertTrue(list.add(e));
    assertEquals(list.toString(), expected);
  }

  @DataProvider(name = "add")
  protected static final Object[][] addTestData() {
    List<Object> list = new ArrayList<>(5);

    Object[][] testData =
        new Object[][] {
          {list, null, "[null]"},
          {list, "dup", "[null, dup]"},
          {list, "dup", "[null, dup, dup]"},
          {list, "str", "[null, dup, dup, str]"},
          {list, null, "[null, dup, dup, str, null]"},
        };
    return testData;
  }
}
