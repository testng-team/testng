package test.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Suite3 {
  public static Test suite() {
    TestSuite suite = new TestSuite("Suite3");
    suite.addTest(Suite4.suite());
    return suite;
  }
}
