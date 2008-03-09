package org.testng.v6;

import org.testng.ClassMethodMap;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.internal.ConfigurationGroupMethods;
import org.testng.xml.XmlTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestPlan {
  ClassMethodMap m_classMethodMap;
  private Map<ITestClass, Set<Object>> m_beforeClassMethods;
  private Set<ITestClass> m_classesSeen = Sets.newHashSet();
  private Set<String> m_groupsSeen = Sets.newHashSet();
  private List<List<ITestNGMethod>> m_sequentialList;
  private List<ITestNGMethod> m_parallelList;
  private List<Operation> m_operations;
  private ConfigurationGroupMethods m_groupMethods;
  private XmlTest m_xmlTest;
  private IRunGroupFactory m_runGroupFactory;
  private ITestNGMethod[] m_beforeSuiteMethods;
  private ITestNGMethod[] m_afterSuiteMethods;

  public TestPlan(
      List<List<ITestNGMethod>> sequentialList,
      List<ITestNGMethod> parallelList, ClassMethodMap cmm, 
      ITestNGMethod[] beforeSuiteMethods, ITestNGMethod[] afterSuiteMethods,
      ConfigurationGroupMethods groupMethods, XmlTest xmlTest)
  {
    m_sequentialList = sequentialList;
    m_parallelList = parallelList;
    m_groupMethods = groupMethods;
    m_xmlTest = xmlTest;
    m_beforeSuiteMethods = beforeSuiteMethods;
    m_afterSuiteMethods = afterSuiteMethods;
    m_sequentialList = sequentialList;
    m_parallelList = parallelList;

    m_classMethodMap = cmm;
    m_beforeClassMethods = cmm.getInvokedBeforeClassMethods();
    m_operations = Lists.newArrayList();
  }
  
  public void init(IRunGroupFactory runGroupFactory) {
    m_runGroupFactory = runGroupFactory;
    for (ITestNGMethod m : m_beforeSuiteMethods) {
      m_operations.add(createOperation(m, m_runGroupFactory));
    }
    
    int affinity = 1;
    for (List<ITestNGMethod> seq : m_sequentialList) {
      for (ITestNGMethod m : seq) {
        addTestOperation(createOperation(m, affinity, m_runGroupFactory));
      }
      affinity++;
      p("  ");
    }
  
    for (ITestNGMethod m : m_parallelList) {
      addTestOperation(createOperation(m, affinity, m_runGroupFactory));
    }
    
    addAfterClassAndGroupsMethods();
    
    for (ITestNGMethod m : m_afterSuiteMethods) {
      m_operations.add(createOperation(m, m_runGroupFactory));
    }
    
//    System.out.println("LIST OF GROUPS:");
//    for (RunGroup r : m_runGroups.keySet()) {
//      System.out.println(r.getName() + ":" + r.getId());
//    }

    for (Operation o : m_operations) {
      System.out.println(o);
    }
    System.out.println("");
  }  
  

  private void addAfterClassAndGroupsMethods() {
    Set<Operation> afterOperations = Sets.newHashSet();

    for (ITestClass cl : m_classesSeen) {
      Integer id = m_runGroupFactory.findRunGroup(RunGroup.CLASS, cl.getName());
      for (ITestNGMethod m : cl.getAfterClassMethods()) {
        Operation o = createOperation(m, 0, m_runGroupFactory);
        o.setAfter(new Integer[] { id });
        afterOperations.add(o);
      }
    }
    
    Map<String, List<ITestNGMethod>> afterGroups = m_groupMethods.getAfterGroupsMethods();
    for (String group : m_groupsSeen) {
      List<ITestNGMethod> afterMethods = afterGroups.get(group);
      Integer id = m_runGroupFactory.findRunGroup(RunGroup.GROUP, group);
      if (afterMethods != null) {
        for (ITestNGMethod m : afterMethods) {
          Operation o = createOperation(m, 0, m_runGroupFactory);
          o.setAfter(new Integer[] { id });
          afterOperations.add(o);
        }
        
      }
    }
    
    addAfterOperations(afterOperations);
  }

  private void addAfterOperations(Set<Operation> afterOperations) {
    Set<Operation> toRemove = Sets.newHashSet();
    for (int i = m_operations.size() - 1; i >= 0; --i) {
      Operation o = m_operations.get(i);
      for (Operation afterOperation : afterOperations) {
        if (afterOperation.mustRunAfter(o)) {
          m_operations.add(i + 1, afterOperation);
          toRemove.add(afterOperation);
        }
      }
      
      for (Operation afterOperation : toRemove) {
        afterOperations.remove(afterOperation);
      }
      
      if (afterOperations.size() == 0) break;
    }
//      Operation o = m_operations.get(i);
//      List<RunGroup> runGroups = o.getRunGroups();
//      for (RunGroup rg : runGroups) {
//        List<Operation> operations = afterOperations.getOperationsThatMustRunAfter(rg.getId());
//        for (Operation operation : operations) {
//          m_operations.add(i + 1, operation);
//        }
//      }
//    }
  }

//  private void addAfterClassAndGroupsMethods() {
//    Set<ITestClass> classesSeen = Sets.newHashSet();
//    Set<String> groupsSeen = Sets.newHashSet();
//
//    for (int i = m_operations.size() - 1; i >= 0; i--) {
//      Operation o = m_operations.get(i);
//      ITestNGMethod m = o.getMethod();
//
//      ITestClass testClass = m.getTestClass();
//      if (! classesSeen.contains(testClass)) {
//        addMethods(testClass.getAfterClassMethods(), o.getAffinity(), i + 1);
//        classesSeen.add(testClass);
//      }
//
//      String[] groups = m.getGroups();
//      for (String group : groups) {
//        Map<String, List<ITestNGMethod>> after = m_groupMethods.getAfterGroupsMap();
//        if (! groupsSeen.contains(group)) {
//          List<ITestNGMethod> methods = after.get(group);
//          addMethods(methods, o.getAffinity(), i + 1);
//          groupsSeen.add(group);
//        }
//      }
//    }
//  }

  /**
   * Add beforeMethod, method, afterMethod
   */
  private void addTestOperation(Operation o) {
    ITestNGMethod method = o.getMethod();
    ITestClass testClass = method.getTestClass();
    if (! m_classesSeen.contains(testClass)) {
      m_classesSeen.add(testClass);
      addMethods(testClass.getBeforeClassMethods(), o.getAffinity(),
          m_operations.size());
    }
    
    String[] groups = method.getGroups();
    for (String group : groups) {
      if (! m_groupsSeen.contains(group)) {
        List<ITestNGMethod> beforeMethods
          = m_groupMethods.getBeforeGroupsMap().get(group); 
        if (beforeMethods != null) {
          ITestNGMethod[] beforeGroupMethods
            = beforeMethods.toArray(new ITestNGMethod[beforeMethods.size()]);
          addMethods(beforeGroupMethods,
              o.getAffinity(), m_operations.size());
          m_groupsSeen.add(group);
        }
      }
    }
    
    addMethods(testClass.getBeforeTestMethods(), o.getAffinity(),
        m_operations.size());
    m_operations.add(o);
    addMethods(testClass.getAfterTestMethods(), o.getAffinity(),
        m_operations.size());
  }
  
  private void addMethods(ITestNGMethod[] methods,
      int affinity, int index)
  {
    for (ITestNGMethod m : methods) {
      m_operations.add(index, createOperation(m, affinity, m_runGroupFactory));
    }
  }

//  private void addMethods(List<ITestNGMethod> methods,
//      int affinity, int index)
//  {
//    addMethods(methods.toArray(new ITestNGMethod[methods.size()]),
//        affinity, index);
//  }


//  private void addMethod(List<Operation> operations, ITestNGMethod m, int affinity) {
//    ITestNGMethod[] beforeMethods = m.getTestClass().getBeforeTestMethods();
//    for (ITestNGMethod bm : beforeMethods) {
//      operations.add(createOperation(bm, affinity));
//    }
//    
//    operations.add(createOperation(m, affinity));
//
//    ITestNGMethod[] afterMethods = m.getTestClass().getAfterTestMethods();
//    for (ITestNGMethod am : afterMethods) {
//      operations.add(createOperation(am, affinity));
//    }
//  }
  
  private void p(String s) {
    System.out.println(s);
  }

  private Operation createOperation(ITestNGMethod m, int affinity, IRunGroupFactory factory) {
    Operation result = new Operation(m, factory, m_xmlTest);
    return result;
  }

  private Operation createOperation(ITestNGMethod m, IRunGroupFactory factory) {
    return createOperation(m, 0, factory);
  }

}
