package test.pholser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:pholser@thoughtworks.com">Paul Holser</a>
 * @version $Id: Demo.java,v 1.3 2006/06/23 10:11:04 cedric Exp $
 */
public class Demo {
  /**
   * @testng.before-class
   */
  public void setUpFixture() {
    Captor.reset();
    Captor.instance().capture( "Demo.setUpFixture" );
  }

  /**
   * @testng.before-method
   */
  public void setUp() {
    Captor.instance().capture( "Demo.setUp" );
  }

  /**
   * @testng.after-method
   */
  public void tearDown() {
    Captor.instance().capture( "Demo.tearDown" );
  }

  /**
   * @testng.after-class
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