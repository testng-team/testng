package test.convert;

import junit.framework.*;

/**
 * This class
 *
 * @author Cedric Beust, May 5, 2004
 * 
 */
public class JUnitSample2 extends TestCase {
  private String m_field = null;
  
  public JUnitSample2() {
    super();
  }
  
  public JUnitSample2(String n) {
    super(n);
  }

   /**   */
  public void setUp() {
    m_field = "foo";
  }
  
  public void testSample2ThatSetUpWasRun() {
    assert null != m_field : "setUp() wasn't run";
  }

}
