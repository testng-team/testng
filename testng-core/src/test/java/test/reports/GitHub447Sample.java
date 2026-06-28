package test.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GitHub447Sample {

  @Test(dataProvider = "add")
  public void add(List<Object> list, Object e, String expected) {
    assertThat(list.add(e)).isTrue();
    assertThat(list.toString()).isEqualTo(expected);
  }

  @DataProvider(name = "add")
  protected static Object[][] addTestData() {
    List<Object> list = new ArrayList<>(5);

    return new Object[][] {
      {list, null, "[null]"},
      {list, "dup", "[null, dup]"},
      {list, "dup", "[null, dup, dup]"},
      {list, "str", "[null, dup, dup, str]"},
      {list, null, "[null, dup, dup, str, null]"},
    };
  }
}
