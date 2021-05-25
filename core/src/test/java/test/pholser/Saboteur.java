package test.pholser;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:pholser@thoughtworks.com">Paul Holser</a>
 * @version $Id: Saboteur.java,v 1.4 2006/06/22 13:45:01 cedric Exp $
 */
public class Saboteur {
  @BeforeClass
  public void setUpFixture() {
    Captor.reset();
    Captor.instance().capture( "Saboteur.setUpFixture" );
  }

  @BeforeMethod
  public void setUp() {
    Captor.instance().capture( "Saboteur.setUp" );
  }

  @AfterMethod
  public void tearDown() {
    Captor.instance().capture( "Saboteur.tearDown" );
  }

  @AfterClass
  public void tearDownFixture() {
    Captor.instance().capture( "Saboteur.tearDownFixture" );
  }

  @Test
  public void go() {
  }
}
