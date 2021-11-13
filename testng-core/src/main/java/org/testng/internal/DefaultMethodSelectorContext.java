package org.testng.internal;

import java.util.Map;
import org.testng.IMethodSelectorContext;
import org.testng.collections.Maps;

/**
 * Simple implementation of IMethodSelectorContext
 *
 * <p>Created on Jan 3, 2007
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class DefaultMethodSelectorContext implements IMethodSelectorContext {
  private final Map<Object, Object> m_userData = Maps.newHashMap();
  private boolean m_isStopped = false;

  @Override
  public Map<Object, Object> getUserData() {
    return m_userData;
  }

  @Override
  public boolean isStopped() {
    return m_isStopped;
  }

  @Override
  public void setStopped(boolean stopped) {
    m_isStopped = stopped;
  }
}
