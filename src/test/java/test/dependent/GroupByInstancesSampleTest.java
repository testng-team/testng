package test.dependent;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.List;

public class GroupByInstancesSampleTest {
  private String m_country;
  public static List<String> m_log;

  private static void log(String method, String country) {
    m_log.add(method + "#" + country);
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
        new Object[] { "usa" },
        new Object[] { "uk" },
    };
  }

  @Factory(dataProvider = "dp")
  public GroupByInstancesSampleTest(String country) {
    m_country = country;
  }

  @Test
  public void signIn() {
    log("signIn", m_country);
  }

  @Test(dependsOnMethods = "signIn")
  public void signOut() {
    log("signOut", m_country);
  }
}
