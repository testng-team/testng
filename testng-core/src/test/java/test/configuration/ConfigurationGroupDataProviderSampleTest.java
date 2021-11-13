package test.configuration;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ConfigurationGroupDataProviderSampleTest {
  static List<Integer> m_list = new ArrayList<>();

  @BeforeGroups(
      groups = {"twice"},
      value = {"twice"})
  public void a() {
    m_list.add(1);
  }

  @Test(
      groups = {"twice"},
      dataProvider = "MyData")
  public void b(int a, int b) {
    m_list.add(2);
  }

  @AfterGroups(
      groups = {"twice"},
      value = {"twice"})
  public void c() {
    m_list.add(3);
  }

  @DataProvider(name = "MyData")
  public Object[][] input() {
    return new Object[][] {{1, 1}, {2, 2}, {3, 3}};
  }
}
