package org.testng.phase;

import org.testng.ITestNGMethod;

public class PhaseMethodEvent extends PhaseEvent {

  private ITestNGMethod m_method;

  public PhaseMethodEvent(String name, boolean before, ITestNGMethod method) {
    super(name, before);
    m_method = method;
  }

  public ITestNGMethod getMethod() {
    return m_method;
  }

  @Override
  public String toString() {
    return "      [PhaseMethodEvent " + getName() + (isBefore() ? " before " : " after ") + "]";
  }
}
