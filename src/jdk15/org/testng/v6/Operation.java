package org.testng.v6;

import org.testng.ITestNGMethod;

public class Operation {

  private ITestNGMethod m_method;
  private Object m_object;
  private Object m_parameters;
  private int m_affinity;
  
  public Operation(ITestNGMethod method) {
    init(method, 0);
  }
  
  public Operation(ITestNGMethod method, int threadAffinity) {
    init(method, threadAffinity);
  }

  private void init(ITestNGMethod method, int affinity) {
    m_method = method;
    m_affinity = affinity;
  }
  
  public ITestNGMethod getMethod() {
    return m_method;
  }

  public String toString() {
    int padding = 0;
    if (m_method.isBeforeClassConfiguration() || m_method.isAfterClassConfiguration()) {
      padding = 2;
    }
    else if (m_method.isBeforeMethodConfiguration() || m_method.isAfterMethodConfiguration()) {
      padding = 6;
    }
    else if (m_method.isBeforeSuiteConfiguration() || m_method.isAfterSuiteConfiguration()) {
      padding = 0;
    }
    else {
      padding = 8;
    }
    
    String p = "";
    for (int i = 0; i < padding; i++) {
      p += " ";
    }
    
    String method = m_method.getTestClass().getName() + "." + m_method.getMethod().getName();
    String result = p + "[" + method + " affinity:" + m_affinity
      + "]";
    
    return result;
  }

  public int getAffinity() {
    return m_affinity;
  }

}
