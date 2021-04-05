package test.sample;

import junit.framework.TestCase;

/**
 * This class
 *
 * @author Cedric Beust, May 5, 2004
 *
 */
public class JUnitSample2 extends TestCase {
  public static final String EXPECTED = "testSample2ThatSetUpWasRun";
  private String m_field = null;

  public JUnitSample2() {
    super();
  }

  public JUnitSample2(String n) {
    super(n);
  }

  @Override
  public void setUp() {
    m_field = "foo";
  }

  public void testSample2ThatSetUpWasRun() {
    assert null != m_field : "setUp() wasn't run";
  }

}
