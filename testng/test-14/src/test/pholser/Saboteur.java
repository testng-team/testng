package test.pholser;



/**
 * @author <a href="mailto:pholser@thoughtworks.com">Paul Holser</a>
 * @version $Id: Saboteur.java,v 1.3 2006/06/23 10:11:04 cedric Exp $
 */
public class Saboteur {
  /**
   * @testng.before-class
   */
  public void setUpFixture() {
    Captor.reset();
    Captor.instance().capture( "Saboteur.setUpFixture" );
  }

  /**
   * @testng.before-method
   */
  public void setUp() {
    Captor.instance().capture( "Saboteur.setUp" );
  }

  /**
   * @testng.after-method
   */
  public void tearDown() {
    Captor.instance().capture( "Saboteur.tearDown" );
  }

  /**
   * @testng.after-class
   */
  public void tearDownFixture() {
    Captor.instance().capture( "Saboteur.tearDownFixture" );
  }

  /**
   * @testng.test
   */
  public void go() {
  }
}
