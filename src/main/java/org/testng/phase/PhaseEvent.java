package org.testng.phase;

public class PhaseEvent {

  private String m_name;
  private boolean m_before;

  public PhaseEvent(String name, boolean before) {
    m_name = name;
    m_before = before;
  }

  public boolean isBefore() {
    return m_before;
  }

  public String getName() {
    return m_name;
  }

}
