package test.configuration;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationGroupInvocationCountSampleTest {
  static List<Integer> m_list = new ArrayList<>();

  @BeforeGroups(groups={"twice"}, value={"twice"})
  public void a(){
    ppp("BEFORE()");
    m_list.add(1);
  }

  @Test(groups = {"twice"}, invocationCount = 3)
  public void b() {
    m_list.add(2);
    ppp("B()");
  }

  @AfterGroups(groups={"twice"}, value={"twice"})
  public void c(){
    m_list.add(3);
    ppp("AFTER()");
  }

  private void ppp(String string) {
    if (false) {
      System.out.println("[A] " + string);
    }
  }


}
