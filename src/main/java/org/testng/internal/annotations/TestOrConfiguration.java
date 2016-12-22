package org.testng.internal.annotations;

import org.testng.annotations.ITestOrConfiguration;

public class TestOrConfiguration extends BaseAnnotation implements ITestOrConfiguration {
  private String[] parameters = {};
  private String[] groups = {};
  private boolean enabled = true;
  private String[] dependsOnGroups = {};
  private String[] dependsOnMethods = {};
  private String description = "";
  private int priority;
  private long timeOut = 0;

  @Override
  public String[] getGroups() {
    return groups;
  }

  @Override
  public boolean getEnabled() {
    return enabled;
  }

  @Override
  public void setDependsOnGroups(String[] dependsOnGroups) {
    this.dependsOnGroups = dependsOnGroups;
  }

  @Override
  public void setDependsOnMethods(String[] dependsOnMethods) {
    this.dependsOnMethods = dependsOnMethods;
  }

  @Override
  public void setGroups(String[] groups) {
    this.groups = groups;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String[] getDependsOnGroups() {
    return dependsOnGroups;
  }

  @Override
  public String[] getDependsOnMethods() {
    return dependsOnMethods;
  }

  @Override
  public String[] getParameters() {
    return parameters;
  }

  public void setParameters(String[] parameters) {
    this.parameters = parameters;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  @Override
  public void setTimeOut(long timeOut) {
    this.timeOut = timeOut;
  }

  @Override
  public long getTimeOut() {
    return timeOut;
  }
}
