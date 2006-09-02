package test.sample;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the suite() functionality of TestNG
 *
 * @author Cedric Beust, May 5, 2004
 * 
 */
public class AllJUnitTests {
  public static Test suite() {
    TestSuite suite= new TestSuite(); 
    suite.addTest(new JUnitSample1("testSample1_1")); 
    suite.addTest(new JUnitSample2("testSample2ThatSetUpWasRun")); 
    return suite;    
  }
}
