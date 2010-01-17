package org.testng.v6;

import org.testng.ITestNGMethod;
import org.testng.xml.XmlTest;

import java.util.List;

public class Operation {

  private ITestNGMethod m_method;
  private Object m_object;
  private Object m_parameters;
  private int m_affinity;
  private List<RunGroup> m_runGroups = Lists.newArrayList();
  private IRunGroupFactory m_runGroupFactory;
  private Integer[] m_after = {};
  private XmlTest m_xmlTest;
  
  public Operation(ITestNGMethod method, IRunGroupFactory runGroupFactory, XmlTest xmlTest) {
    init(method, 0, runGroupFactory, xmlTest);
  }
  
  public Operation(ITestNGMethod method, int threadAffinity, IRunGroupFactory runGroupFactory,
      XmlTest xmlTest) 
  {
    init(method, threadAffinity, runGroupFactory, xmlTest);
  }

  private void init(ITestNGMethod method, int affinity, IRunGroupFactory runGroupFactory,
      XmlTest xmlTest) 
  {
    m_method = method;
    m_affinity = affinity;
    m_runGroupFactory = runGroupFactory;
    m_xmlTest = xmlTest;

    m_runGroups.add(m_runGroupFactory.getRunGroup(RunGroup.CLASS,
        method.getTestClass().getRealClass().getName()));

    m_runGroups.add(m_runGroupFactory.getRunGroup(RunGroup.XML_TEST, m_xmlTest.getName()));

    for (String group : method.getGroups()) {
      m_runGroups.add(m_runGroupFactory.getRunGroup(RunGroup.GROUP, group));
    }
  }
  
  public List<RunGroup> getRunGroups() {
    return m_runGroups;
  }
  
  public ITestNGMethod getMethod() {
    return m_method;
  }

  public String toString() {
    String padding;
    if (m_method.isBeforeClassConfiguration() || m_method.isAfterClassConfiguration()) {
      padding = "    ";
    }
    else if (m_method.isBeforeGroupsConfiguration() || m_method.isAfterGroupsConfiguration()) {
      padding = "        ";
    }
    else if (m_method.isBeforeMethodConfiguration() || m_method.isAfterMethodConfiguration()) {
      padding = "            ";
    }
    else if (m_method.isBeforeSuiteConfiguration() || m_method.isAfterSuiteConfiguration()) {
      padding = "";
    }
    else {
      padding = "                -- ";
    }
    
//    String p = "";
//    for (int i = 0; i < padding; i++) {
//      p += " ";
//    }
    
    String after = "";
    if (m_after.length > 0) {
      after = "after:";
      for (int i : m_after) {
        after += i + " ";
      }
    }
    
    String method = m_method.getTestClass().getName() + "." + m_method.getMethod().getName();
    String result = padding + "[" + method + " affinity:" + m_affinity
      + " groups:" + m_runGroups
      + after
      + "]";
    
    return result;
  }

  public int getAffinity() {
    return m_affinity;
  }
  
  public boolean mustRunAfter(Operation o) {
    List<RunGroup> runGroups = o.getRunGroups();
    for (int after : m_after) {
      for (RunGroup rg : runGroups) {
        if (rg.getId() == after) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * @return the array of RunGroups we must run after, or an empty array if not applicable.
   */
  public Integer[] getAfter() {
    return m_after;
  }
  
  public void setAfter(Integer[] after) {
    m_after = after;
  }

}
