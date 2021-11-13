package test.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Suite4 {
  public static Test suite() {
    TestSuite suite = new TestSuite("Suite4");
    suite.addTestSuite(TestAe.class);
    suite.addTestSuite(TestAf.class);
    return suite;
  }
}
