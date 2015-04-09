package test.dependent;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@Test
public class DepBugSampleTest {
  private static List<String> m_log = new ArrayList<>();

  private static void log(String s) {
//    ppp(s);
    m_log.add(s);
  }

  private static void ppp(String s) {
    System.out.println("[DepBugSampleTest] " + s);
  }

  public static List<String> getLog() {
    return m_log;
  }

  @BeforeClass
  public void setup() throws Exception {
    log("setup");
  }

  @AfterClass
  public void destroy() throws Exception {
    log("destroy");
  }

  @Test(dependsOnMethods = "send")
  public void get() throws Exception {
    log("get");
  }

  public void send() throws Exception {
    log("send");
  }

}
