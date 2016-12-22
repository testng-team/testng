package org.testng.internal.annotations;

public class BaseBeforeAfter extends TestOrConfiguration implements IBaseBeforeAfter {
  private String[] parameters = {};
  private boolean alwaysRun = false;
  private boolean inheritGroups = true;
  private String[] beforeGroups = {};
  private String[] afterGroups = {};
  private String description;

  /**
   * @return the description
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  public void setAlwaysRun(boolean alwaysRun) {
    this.alwaysRun = alwaysRun;
  }

  public void setInheritGroups(boolean inheritGroups) {
    this.inheritGroups = inheritGroups;
  }

  @Override
  public void setParameters(String[] parameters) {
    this.parameters = parameters;
  }

  @Override
  public String[] getParameters() {
    return parameters;
  }

  @Override
  public boolean getAlwaysRun() {
    return alwaysRun;
  }

  @Override
  public boolean getInheritGroups() {
    return inheritGroups;
  }

  public String[] getAfterGroups() {
    return afterGroups;
  }

  public void setAfterGroups(String[] afterGroups) {
    this.afterGroups = afterGroups;
  }

  public String[] getBeforeGroups() {
    return beforeGroups;
  }

  public void setBeforeGroups(String[] beforeGroups) {
    this.beforeGroups = beforeGroups;
  }

}
