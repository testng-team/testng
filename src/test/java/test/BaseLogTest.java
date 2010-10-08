package test;

import org.testng.annotations.BeforeSuite;
import org.testng.collections.Lists;

import java.util.List;

/**
 * Base class for tests that need to log methods as they get called.
 *
 * @author cbeust
 */
public class BaseLogTest {
  private static List<String> m_log;

  @BeforeSuite
  public void bc() {
    m_log = Lists.newArrayList();
  }

  public static void log(String s) {
    m_log.add(s);
  }

  public static List<String> getLog() {
    return m_log;
  }
}
