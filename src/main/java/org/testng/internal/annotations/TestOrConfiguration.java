package org.testng.internal.annotations;

import org.testng.annotations.ITestOrConfiguration;

public class TestOrConfiguration
  extends BaseAnnotation
  implements ITestOrConfiguration
{
  private String[] m_parameters = {};
  private String[] m_groups = {};
  private boolean m_enabled = true;
  private String[] m_dependsOnGroups = {};
  private String[] m_dependsOnMethods = {};
  private String m_description = "";
  private int m_priority;
  private long m_timeOut = 0;

  @Override
  public String[] getGroups() {
    return m_groups;
  }

  @Override
  public boolean getEnabled() {
    return m_enabled;
  }

  @Override
  public void setDependsOnGroups(String[] dependsOnGroups) {
    m_dependsOnGroups = dependsOnGroups;
  }

  @Override
  public void setDependsOnMethods(String[] dependsOnMethods) {
    m_dependsOnMethods = dependsOnMethods;
  }

  @Override
  public void setGroups(String[] groups) {
    m_groups = groups;
  }

  @Override
  public String getDescription() {
    return m_description;
  }

  @Override
  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  @Override
  public String[] getDependsOnGroups() {
    return m_dependsOnGroups;
  }

  @Override
  public String[] getDependsOnMethods() {
    return m_dependsOnMethods;
  }

  @Override
  public String[] getParameters() {
    return m_parameters;
  }

  public void setParameters(String[] parameters) {
    m_parameters = parameters;
  }

  @Override
  public void setDescription(String description) {
    m_description = description;
  }

  public int getPriority() {
    return m_priority;
  }

  public void setPriority(int priority) {
    m_priority = priority;
  }

  @Override
  public void setTimeOut(long timeOut) {
    m_timeOut = timeOut;
  }

  @Override
  public long getTimeOut() {
    return m_timeOut;
  }
}
