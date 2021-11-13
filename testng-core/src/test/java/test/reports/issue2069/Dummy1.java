package test.reports.issue2069;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Dummy1 extends TestSuite {

  public static Test suite() {
    final TestSuite suite = new TestSuite("Failing report");
    suite.addTestSuite(Dummy3.class);
    return suite;
  }
}
