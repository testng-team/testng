package test.pholser;



/**
 * @author <a href="mailto:pholser@thoughtworks.com">Paul Holser</a>
 * @version $Id: Saboteur.java,v 1.1 2006/06/15 22:27:38 cedric Exp $
 */
public class Saboteur {
  /**
   * @testng.configuration beforeTestClass="true"
   */
  public void setUpFixture() {
    Captor.reset();
    Captor.instance().capture( "Saboteur.setUpFixture" );
  }

  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void setUp() {
    Captor.instance().capture( "Saboteur.setUp" );
  }

  /**
   * @testng.configuration afterTestMethod="true"
   */
  public void tearDown() {
    Captor.instance().capture( "Saboteur.tearDown" );
  }

  /**
   * @testng.configuration afterTestClass="true"
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
