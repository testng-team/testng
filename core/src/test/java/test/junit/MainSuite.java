package test.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

public class MainSuite {
  public static Test suite() {
    TestSuite suite = new TestSuite("MainSuite");

    suite.addTest(Suite1.suite());
    suite.addTest(Suite2.suite());
    suite.addTest(Suite3.suite());

    return suite;
  }

}
