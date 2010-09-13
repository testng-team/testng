package org.testng.phase;

import org.testng.ITestClass;

public class PhaseClassEvent extends PhaseEvent {
  private ITestClass m_testClass;

  public PhaseClassEvent(String name, boolean before, ITestClass cls) {
    super(name, before);
    m_testClass = cls;
  }

  public ITestClass getTestClass() {
    return m_testClass;
  }

  @Override
  public String toString() {
    return "    [PhaseClassEvent " + getName() + (isBefore() ? " before " : " after ") + "]";
  }

}
