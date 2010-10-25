package test.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Suite1 {
  public Suite1(String s) {
    // dummy
  }

  public static Test suite() {
    TestSuite suite = new TestSuite("JUnitSuite1");
    suite.addTestSuite(TestAa.class);
    suite.addTestSuite(TestAb.class);
    return suite;
  }
}
