package org.testng.v6;

import java.util.Map;

public class SuitePlan implements IRunGroupFactory {
  private Map<RunGroup, Integer> m_runGroups = Maps.newHashMap();
  private int m_currentGroupId = 1;

  public RunGroup getRunGroup(int type, String name) {
    RunGroup result = new RunGroup(type, name, m_currentGroupId);
    Integer id = m_runGroups.get(result);
    if (id == null) {
      m_runGroups.put(result, m_currentGroupId);
      m_currentGroupId++;
    }
    else {
      result.setId(id);
    }
    return result;
  }

  public Integer findRunGroup(int type, String name) {
    return m_runGroups.get(new RunGroup(type, name, 0));
  }

}
