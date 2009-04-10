package test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.testng.Assert;

public class BaseDistributedTest {
  private boolean m_verbose = false;
  
  protected void verifyTests(String title, String[] exp, Map found) {
    Map expected = new HashMap();
    for(int i = 0; i < exp.length; i++) {
      expected.put(exp[i], exp[i]);
    }

    Assert.assertEquals(found.size(), expected.size(),
        "Verification for " + title + " tests failed:");

    for(Iterator it = expected.values().iterator(); it.hasNext();) {
      String name = (String) it.next();
      if(null == found.get(name)) {
        dumpMap("Expected", expected);
        dumpMap("Found", found);
      }
      
      Assert.assertNotNull(found.get(name),
          "Expected to find method " + name + " in " + title
          + " but didn't find it.");
    }
  }

  protected void dumpMap(String title, Map m) {
    if (m_verbose) {
      System.out.println("==== " + title);
      for(Iterator it = m.keySet().iterator(); it.hasNext();) {
        Object key = it.next();
        Object value = m.get(key);
        ppp(key + "  => " + value);
      }
    }
  }
  private void ppp(String s) {
    if (m_verbose) {
      System.out.println("[BaseDistributedTest] " + s);
    }
  }


}
