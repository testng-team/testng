package org.testng.v6;

import org.testng.ClassMethodMap;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestPlan {
  ClassMethodMap m_classMethodMap;
  private Map<ITestClass, Set<Object>> m_beforeClassMethods;
  private Set<ITestClass> m_classesSeen = Sets.newHashSet();
  private List<List<ITestNGMethod>> m_sequentialList;
  private List<ITestNGMethod> m_parallelList;
  private List<Operation> m_operations;

  public TestPlan(List<List<ITestNGMethod>> sequentialList,
      List<ITestNGMethod> parallelList, ClassMethodMap cmm, 
      ITestNGMethod[] beforeSuiteMethods, ITestNGMethod[] afterSuiteMethods)
  {
    m_sequentialList = sequentialList;
    m_parallelList = parallelList;

    m_classMethodMap = cmm;
    m_beforeClassMethods = cmm.getInvokedBeforeClassMethods();
    m_operations = Lists.newArrayList();
    
    for (ITestNGMethod m : beforeSuiteMethods) {
      m_operations.add(new Operation(m));
    }
    
    int affinity = 1;
    for (List<ITestNGMethod> seq : sequentialList) {
      for (ITestNGMethod m : seq) {
        addTestOperation(new Operation(m, affinity));
      }
      affinity++;
      p("  ");
    }
  
    for (ITestNGMethod m : parallelList) {
      addTestOperation(new Operation(m, affinity));
    }
    
    addAfterClassMethods();
    
    for (ITestNGMethod m : afterSuiteMethods) {
      m_operations.add(new Operation(m));
    }
    
    for (Operation o : m_operations) {
      System.out.println(o);
    }
    System.out.println("");
  }  
  
  private void addAfterClassMethods() {
    Set<ITestClass> m_seen = Sets.newHashSet();
    for (int i = m_operations.size() - 1; i >= 0; i--) {
      Operation o = m_operations.get(i);
      ITestNGMethod m = o.getMethod();
      ITestClass testClass = m.getTestClass();
      if (! m_seen.contains(testClass)) {
        addClassMethods(testClass.getAfterClassMethods(), o.getAffinity(), i + 1);
        m_seen.add(testClass);
      }
    }
  }

  /**
   * Add beforeMethod, method, afterMethod
   */
  private void addTestOperation(Operation o) {
    ITestClass testClass = o.getMethod().getTestClass();
    if (! m_classesSeen.contains(testClass)) {
      m_classesSeen.add(testClass);
      addClassMethods(testClass.getBeforeClassMethods(), o.getAffinity(),
          m_operations.size());
    }
    addClassMethods(testClass.getBeforeTestMethods(), o.getAffinity(),
        m_operations.size());
    m_operations.add(o);
    addClassMethods(testClass.getAfterTestMethods(), o.getAffinity(),
        m_operations.size());
  }
  
  private void addClassMethods(ITestNGMethod[] methods,
      int affinity, int index)
  {
    for (ITestNGMethod m : methods) {
      m_operations.add(index, new Operation(m, affinity));
    }
  }


//  private void addMethod(List<Operation> operations, ITestNGMethod m, int affinity) {
//    ITestNGMethod[] beforeMethods = m.getTestClass().getBeforeTestMethods();
//    for (ITestNGMethod bm : beforeMethods) {
//      operations.add(new Operation(bm, affinity));
//    }
//    
//    operations.add(new Operation(m, affinity));
//
//    ITestNGMethod[] afterMethods = m.getTestClass().getAfterTestMethods();
//    for (ITestNGMethod am : afterMethods) {
//      operations.add(new Operation(am, affinity));
//    }
//  }
  
  private void p(String s) {
    System.out.println(s);
  }
}
