package test.pholser;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:pholser@thoughtworks.com">Paul Holser</a>
 * @version $Id: Demo.java,v 1.5 2006/06/22 13:45:01 cedric Exp $
 */
public class Demo {
  @BeforeClass
  public void setUpFixture() {
    Captor.reset();
    Captor.instance().capture( "Demo.setUpFixture" );
  }

  @BeforeMethod
  public void setUp() {
    Captor.instance().capture( "Demo.setUp" );
  }

  @AfterMethod
  public void tearDown() {
    Captor.instance().capture( "Demo.tearDown" );
  }

  @AfterClass
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