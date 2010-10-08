package org.testng;

/**
 * This class defines a pair of instance/class.  A method with @Factory
 * can return an array of these objects instead of Object[] so that
 * instances can be dynamic proxies or mock objects and still provide
 * enough information to TestNG to figure out what classes the
 * annotations should be looked up in.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IInstanceInfo {

  /**
   * @return The instance on which the tests will be invoked.
   */
  public Object getInstance();

  /**
   * @return The class on which the TestNG annotations should be looked for.
   */
  public Class getInstanceClass();
}
