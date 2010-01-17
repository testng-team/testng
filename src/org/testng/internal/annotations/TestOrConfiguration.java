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
  
  public String[] getGroups() {
    return m_groups;
  }

  public boolean getEnabled() {
    return m_enabled;
  }

  public void setDependsOnGroups(String[] dependsOnGroups) {
    m_dependsOnGroups = dependsOnGroups;
  }

  public void setDependsOnMethods(String[] dependsOnMethods) {
    m_dependsOnMethods = dependsOnMethods;
  }

  public void setGroups(String[] groups) {
    m_groups = groups;
  }

  public String getDescription() {
    return m_description;
  }

  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }
  
  public String[] getDependsOnGroups() {
    return m_dependsOnGroups;
  }

  public String[] getDependsOnMethods() {
    return m_dependsOnMethods;
  }
  
  public String[] getParameters() {
    return m_parameters;
  }
  
  public void setParameters(String[] parameters) {
    m_parameters = parameters;
  }

  public void setDescription(String description) {
    m_description = description;
  }

}
