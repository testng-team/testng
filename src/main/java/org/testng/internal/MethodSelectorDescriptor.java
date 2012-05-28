package org.testng.internal;

import org.testng.IMethodSelector;
import org.testng.ITestNGMethod;

import java.util.List;

/**
 * This class describes a method selector:
 * - The class that implements it
 * - Its priority
 *
 * Created on Sep 26, 2005
 * @author cbeust
 */
public class MethodSelectorDescriptor implements Comparable<MethodSelectorDescriptor> {
  private IMethodSelector m_methodSelector;
  private int m_priority;

  public int getPriority() {
    return m_priority;
  }

  public IMethodSelector getMethodSelector() {
    return m_methodSelector;
  }

  public MethodSelectorDescriptor(IMethodSelector selector, int priority) {
    m_methodSelector = selector;
    m_priority = priority;
  }

  @Override
  public int compareTo(MethodSelectorDescriptor other) {
    int result = 0;

    try {
      int p1 = getPriority();
      int p2 = other.getPriority();
      result = p1 - p2;
    }
    catch(Exception ex) {
      // ignore
    }

    return result;
  }

  public void setTestMethods(List<ITestNGMethod> testMethods) {
    m_methodSelector.setTestMethods(testMethods);

  }
}
