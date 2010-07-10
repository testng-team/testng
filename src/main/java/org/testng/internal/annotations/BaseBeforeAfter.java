package org.testng.internal.annotations;

public class BaseBeforeAfter 
  extends TestOrConfiguration 
  implements IBaseBeforeAfter 
{
  private String[] m_parameters = {};
  private boolean m_alwaysRun = false;
  private boolean m_inheritGroups = true;
  private String[] m_beforeGroups = {};
  private String[] m_afterGroups = {};
  private String m_description;

  /**
   * @return the description
   */
  public String getDescription() {
    return m_description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    m_description = description;
  }

  public void setAlwaysRun(boolean alwaysRun) {
    m_alwaysRun = alwaysRun;
  }

  public void setInheritGroups(boolean inheritGroups) {
    m_inheritGroups = inheritGroups;
  }

  public void setParameters(String[] parameters) {
    m_parameters = parameters;
  }

  public String[] getParameters() {
    return m_parameters;
  }

  public boolean getAlwaysRun() {
    return m_alwaysRun;
  }

  public boolean getInheritGroups() {
    return m_inheritGroups;
  }

  public String[] getAfterGroups() {
    return m_afterGroups;
  }

  public void setAfterGroups(String[] afterGroups) {
    m_afterGroups = afterGroups;
  }

  public String[] getBeforeGroups() {
    return m_beforeGroups;
  }

  public void setBeforeGroups(String[] beforeGroups) {
    m_beforeGroups = beforeGroups;
  }

}
