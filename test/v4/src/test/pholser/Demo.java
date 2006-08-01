package test.pholser;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:pholser@thoughtworks.com">Paul Holser</a>
 * @version $Id: Demo.java,v 1.1 2006/06/15 22:20:09 cedric Exp $
 */
public class Demo {
  @Configuration(beforeTestClass = true)
  public void setUpFixture() {
    Captor.reset();
    Captor.instance().capture( "Demo.setUpFixture" );
  }

  @Configuration(beforeTestMethod = true)
  public void setUp() {
    Captor.instance().capture( "Demo.setUp" );
  }

  @Configuration(afterTestMethod = true)
  public void tearDown() {
    Captor.instance().capture( "Demo.tearDown" );
  }

  @Configuration(afterTestClass = true)
  public void tearDownFixture() {
    final List<String> expected = Arrays.asList(new String[] { "Demo.setUpFixture", "Demo.setUp", "Demo.tearDown" });
    final List<String> actual = Captor.instance().captives();
    verify(expected, actual);
  }

  @Test
  public void go() {
    final List<String> expected = Arrays.asList(new String[] { "Demo.setUpFixture", "Demo.setUp" } );
    final List<String> actual = Captor.instance().captives();
    verify(expected, actual);
  }
  
  private void verify(List<String> expected, List<String> actual) {
    if (! expected.equals(actual)) {
      throw new AssertionError("\nExpected:" + dumpList(expected) + "\n     Got:" + dumpList(actual));
    }
  }
  
  private String dumpList(List<String> list) {
    StringBuffer result = new StringBuffer();
    for (String l : list) {
      result.append(" " + l);
    }
    
    return result.toString();
  }
}