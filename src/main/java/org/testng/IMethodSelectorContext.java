package org.testng;

import java.util.Map;

/**
 * An implementation of this interface is passed to all the Method Selectors
 * when their includeMethod() is invoked.  Method selectors can invoke
 * any method of this context at that time.
 *
 * Created on Jan 3, 2007
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IMethodSelectorContext {

  /**
   * @return true if no more Method Selectors should be invoked after
   * the current one.
   */
  public boolean isStopped();

  /**
   * Indicate that no other Method Selectors should be invoked after the
   * current one if stopped is false.
   * @param stopped
   */
  public void setStopped(boolean stopped);

  /**
   * @return a Map that can be freely manipulated by the Method Selector.
   * This can be used to share information among several Method Selectors.
   */
  public Map<Object, Object> getUserData();
}
