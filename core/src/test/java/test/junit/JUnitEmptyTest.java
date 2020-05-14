package test.junit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class JUnitEmptyTest extends TestCase {

  public JUnitEmptyTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite s = new TestSuite(JUnitEmptyTest.class);
    return s;
  }
}