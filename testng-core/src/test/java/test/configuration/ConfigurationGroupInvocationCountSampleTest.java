package test.configuration;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class ConfigurationGroupInvocationCountSampleTest {
  static List<Integer> m_list = new ArrayList<>();

  @BeforeGroups(
      groups = {"twice"},
      value = {"twice"})
  public void a() {
    m_list.add(1);
  }

  @Test(
      groups = {"twice"},
      invocationCount = 3)
  public void b() {
    m_list.add(2);
  }

  @AfterGroups(
      groups = {"twice"},
      value = {"twice"})
  public void c() {
    m_list.add(3);
  }
}
