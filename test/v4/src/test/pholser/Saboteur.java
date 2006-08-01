package test.pholser;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:pholser@thoughtworks.com">Paul Holser</a>
 * @version $Id: Saboteur.java,v 1.1 2006/06/15 22:20:10 cedric Exp $
 */
public class Saboteur {
  @Configuration(beforeTestClass = true)
  public void setUpFixture() {
    Captor.reset();
    Captor.instance().capture( "Saboteur.setUpFixture" );
  }

  @Configuration(beforeTestMethod = true)
  public void setUp() {
    Captor.instance().capture( "Saboteur.setUp" );
  }

  @Configuration(afterTestMethod = true)
  public void tearDown() {
    Captor.instance().capture( "Saboteur.tearDown" );
  }

  @Configuration(afterTestClass = true)
  public void tearDownFixture() {
    Captor.instance().capture( "Saboteur.tearDownFixture" );
  }

  @Test
  public void go() {
  }
}
