package org.testng.internal;

import java.util.List;
import javax.annotation.Nonnull;
import org.testng.IMethodSelector;
import org.testng.ITestNGMethod;

/** This class describes a method selector: - The class that implements it - Its priority */
public class MethodSelectorDescriptor implements Comparable<MethodSelectorDescriptor> {

  private final IMethodSelector m_methodSelector;
  private final int m_priority;

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
  public int compareTo(@Nonnull MethodSelectorDescriptor other) {
    return m_priority - other.m_priority;
  }

  public void setTestMethods(List<ITestNGMethod> testMethods) {
    m_methodSelector.setTestMethods(testMethods);
  }
}
