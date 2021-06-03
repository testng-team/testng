package test.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Suite2 {
  public static Test suite() {
    TestSuite suite = new TestSuite("Suite2");
    suite.addTestSuite(TestAc.class);
    suite.addTestSuite(TestAd.class);
    suite.addTest(Suite3.suite());
    return suite;
  }
}
