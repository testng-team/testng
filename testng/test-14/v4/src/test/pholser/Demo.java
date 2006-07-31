package test.pholser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:pholser@thoughtworks.com">Paul Holser</a>
 * @version $Id: Demo.java,v 1.1 2006/06/15 22:27:38 cedric Exp $
 */
public class Demo {
  /**
   * @testng.configuration beforeTestClass="true"
   */
  public void setUpFixture() {
    Captor.reset();
    Captor.instance().capture( "Demo.setUpFixture" );
  }

  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void setUp() {
    Captor.instance().capture( "Demo.setUp" );
  }

  /**
   * @testng.configuration afterTestMethod="true"
   */
  public void tearDown() {
    Captor.instance().capture( "Demo.tearDown" );
  }

  /**
   * @testng.configuration afterTestClass="true"
   */
  public void tearDownFixture() {
    final List expected = Arrays.asList(new String[] { "Demo.setUpFixture", "Demo.setUp", "Demo.tearDown" });
    final List actual = Captor.instance().captives();
    verify(expected, actual);
  }

  /**
   * @testng.test
   */
  public void go() {
    final List expected = Arrays.asList(new String[] { "Demo.setUpFixture", "Demo.setUp" } );
    final List actual = Captor.instance().captives();
    verify(expected, actual);
  }


  private void verify(List expected, List actual) {
    if (! expected.equals(actual)) {
      throw new AssertionError("\nExpected:" + dumpList(expected) + "\n     Got:" + dumpList(actual));
    }
  }
  
  private String dumpList(List list) {
    StringBuffer result = new StringBuffer();
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      result.append(" " + it.next());
    }
    
    return result.toString();
  }
}