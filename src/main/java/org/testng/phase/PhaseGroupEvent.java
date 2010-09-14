package org.testng.phase;


public class PhaseGroupEvent extends PhaseEvent {
  private String m_groupName;

  public PhaseGroupEvent(String name, boolean before, String groupName) {
    super(name, before);
    m_groupName = groupName;
  }

  public String getGroupName() {
    return m_groupName;
  }

  @Override
  public String toString() {
    return "    [PhaseGroupEvent " + getName() + (isBefore() ? " before " : " after ") + "]";
  }

}
