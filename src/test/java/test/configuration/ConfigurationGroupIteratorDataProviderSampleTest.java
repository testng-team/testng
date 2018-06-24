package test.configuration;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ConfigurationGroupIteratorDataProviderSampleTest {
  static List<Integer> m_list = new ArrayList<>();

  @BeforeGroups(
      groups = {"twice"},
      value = {"twice"})
  public void a() {
    ppp("BEFORE()");
    m_list.add(1);
  }

  @Test(
      groups = {"twice"},
      dataProvider = "MyData")
  public void b(int a, int b) {
    m_list.add(2);
    ppp("B()" + a + "," + b);
  }

  @AfterGroups(
      groups = {"twice"},
      value = {"twice"})
  public void c() {
    m_list.add(3);
    ppp("AFTER()");
  }

  @DataProvider(name = "MyData")
  public Iterator<Object[]> input() {
    return Arrays.asList(new Object[] {1, 1}, new Object[] {2, 2}, new Object[] {3, 3}).iterator();
  }

  private void ppp(String string) {
    if (false) {
      System.out.println("[A] " + string);
    }
  }
}
