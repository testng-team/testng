package test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.testng.ITestResult;
import org.testng.collections.Maps;
import org.testng.log4testng.Logger;

public class BaseDistributedTest {
  private boolean m_verbose = false;

  protected void verifyTests(String title, String[] exp, Map<String, List<ITestResult>> found) {
    Map<String, String> expected = Maps.newHashMap();
    for (String element : exp) {
      expected.put(element, element);
    }

    assertThat(found.size())
        .withFailMessage("Verification for " + title + " tests failed:")
        .isEqualTo(expected.size());

    for (String o : expected.values()) {
      if (null == found.get(o)) {
        dumpMap("Expected", expected);
        dumpMap("Found", found);
      }

      assertThat(found.get(o))
          .withFailMessage("Expected to find method " + o + " in " + title + " but didn't find it.")
          .isNotNull();
    }
  }

  protected void dumpMap(String title, Map<?, ?> m) {
    if (m_verbose) {
      Logger.getLogger(getClass()).info("==== " + title);
      for (Map.Entry<?, ?> entry : m.entrySet()) {
        log(entry.getKey() + "  => " + entry.getValue());
      }
    }
  }

  private void log(String s) {
    if (m_verbose) {
      Logger.getLogger(getClass()).info("[BaseDistributedTest] " + s);
    }
  }
}
