package org.testng.internal;

import java.util.HashMap;
import java.util.Map;

import org.testng.IMethodSelectorContext;

/**
 * Simple implementation of IMethodSelectorContext
 * 
 * Created on Jan 3, 2007
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class DefaultMethodSelectorContext implements IMethodSelectorContext {
  private Map<Object, Object> m_userData = new HashMap<Object, Object>();
  private boolean m_isStopped = false;

  public Map<Object, Object> getUserData() {
    return m_userData;
  }

  public boolean isStopped() {
    return m_isStopped;
  }

  public void setStopped(boolean stopped) {
    m_isStopped = stopped;
  }

}
