package test.configuration;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigurationGroupBothSampleTest {
  static List<Integer> m_list = Collections.synchronizedList(new ArrayList<Integer>());

  private synchronized static void addToList(Integer n) {
    m_list.add(n);
  }

  @BeforeGroups(groups={"twice"}, value={"twice"})
  public void a(){
    ppp("BEFORE()");
    addToList(1);
  }

  @Test(groups={"twice"}, dataProvider="MyData", invocationCount = 2, threadPoolSize=2)
  public void b(int a, int b) {
    addToList(2);
    ppp("B()"  + a + "," + b);
  }

  @AfterGroups(groups={"twice"}, value={"twice"})
  public void c(){
    addToList(3);
    ppp("AFTER()");
  }

  @DataProvider(name="MyData")
  public Object[][] input(){
    return new Object[][]{ {1,1}, {2,2}, {3,3}};
  }

  private void ppp(String string) {
    if (false) {
      System.out.println("[A] " + string + " on Thread:" + Thread.currentThread());
    }
  }


}
