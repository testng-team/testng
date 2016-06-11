package test.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CartesianProductTest {

  @DataProvider
  public Object[][] data() {
    return new Object[][] {
        new Object[] { "one" },
        new Object[] { "two" },
        new Object[] { "three" }
    };
  }

  @DataProvider(indices = { 0, 2 })
  public Object[][] filteredData() {
    return new Object[][] {
        new Object[] { 1 },
        new Object[] { 2 },
        new Object[] { 3 }
    };
  }

  @DataProvider(name = "iterator")
  public Iterator<Object[]> iteratorData() {
    return new ArrayList<Object[]>() {{
        add(new Object[] { "a" });
        add(new Object[] { "b" });
        add(new Object[] { "c" });
    }}.iterator();
  }

  private Set<String> cartesianProducts = new HashSet<>();
  @Test(dataProvider = { "data", "filteredData", "iterator" })
  public void testOrder(String data, int filtered, String iterator) {
    cartesianProducts.add(String.format("%s:%d:%s", data, filtered, iterator));
  }

  @Test(dependsOnMethods = "testOrder")
  public void verifyOrder() {
    assertThat(cartesianProducts).containsOnly(
        "one:1:a",
        "one:3:a",
        "one:1:b",
        "one:3:b",
        "one:1:c",
        "one:3:c",
        "two:1:a",
        "two:3:a",
        "two:1:b",
        "two:3:b",
        "two:1:c",
        "two:3:c",
        "three:1:a",
        "three:3:a",
        "three:1:b",
        "three:3:b",
        "three:1:c",
        "three:3:c"
    );
  }
}
