package org.testng.v6;

import org.testng.ITestNGMethod;
import org.testng.xml.XmlTest;

import java.util.List;
import java.util.Map;

public class SuitePlan implements IRunGroupFactory {
  private Map<RunGroup, Integer> m_runGroups = Maps.newHashMap();
  private int m_currentGroupId = 1;
  private List<TestPlan> m_testPlans = Lists.newArrayList();
  private List<Operation> m_operations;
  private List<ITestNGMethod> m_beforeSuiteMethods = Lists.newArrayList();
  private List<ITestNGMethod> m_afterSuiteMethods = Lists.newArrayList();

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
  
  public void init() {
    for (ITestNGMethod m : m_beforeSuiteMethods) {
      m_operations.add(createOperation(m, this));
    }

  }
  
  private Operation createOperation(ITestNGMethod m, int affinity,
      IRunGroupFactory factory, XmlTest xmlTest) 
  {
    Operation result = new Operation(m, factory, xmlTest);
    return result;
  }

  private Operation createOperation(ITestNGMethod m, IRunGroupFactory factory) {
    return createOperation(m, 0, factory, null);
  }
  
  public void dump() {
    p("RUN GROUPS:");
    for (RunGroup rg : m_runGroups.keySet()) {
      p("  " + rg);
    }
    
    p("");
    p("PLANS:");
    for (TestPlan tp : m_testPlans) {
      p(tp.toString());
    }
  }

  private void p(String string) {
    System.out.println(string);
  }

  public void addTestPlan(TestPlan testPlan) {
    testPlan.init(this);
    m_testPlans.add(testPlan);
  }

}
