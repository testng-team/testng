package test.annotationtransformer;

import org.testng.internal.annotations.BaseAnnotation;
import org.testng.internal.annotations.ITest;

public class TestDelegateAnnotation 
  extends BaseAnnotation
  implements ITest 
{
  private ITest m_annotation;

  public TestDelegateAnnotation(ITest annotation) {
    m_annotation = annotation;
  }

  public boolean getAlwaysRun() {
    return m_annotation.getAlwaysRun();
  }

  public String getDataProvider() {
    return m_annotation.getDataProvider();
  }

  public String[] getDependsOnGroups() {
    return m_annotation.getDependsOnGroups();
  }

  public String[] getDependsOnMethods() {
    return m_annotation.getDependsOnMethods();
  }

  public String getDescription() {
    return m_annotation.getDescription();
  }

  public boolean getEnabled() {
    return m_annotation.getEnabled();
  }

  public Class[] getExpectedExceptions() {
    return m_annotation.getExpectedExceptions();
  }

  public String[] getGroups() {
    return m_annotation.getGroups();
  }

  public int getInvocationCount() {
    return m_annotation.getInvocationCount();
  }

  public String[] getParameters() {
    return m_annotation.getParameters();
  }

  public boolean getSequential() {
    return m_annotation.getSequential();
  }

  public int getSuccessPercentage() {
    return m_annotation.getSuccessPercentage();
  }

  public String getSuiteName() {
    return m_annotation.getSuiteName();
  }

  public String getTestName() {
    return m_annotation.getTestName();
  }

  public int getThreadPoolSize() {
    return m_annotation.getThreadPoolSize();
  }

  public long getTimeOut() {
    return m_annotation.getTimeOut();
  }

  public void setDependsOnGroups(String[] dependsOnGroups) {
    m_annotation.setDependsOnGroups(dependsOnGroups);
  }

  public void setDependsOnMethods(String[] dependsOnMethods) {
    m_annotation.setDependsOnMethods(dependsOnMethods);
  }

  public void setEnabled(boolean enabled) {
    m_annotation.setEnabled(enabled);
  }

  public void setGroups(String[] groups) {
    m_annotation.setGroups(groups);
  }

}
