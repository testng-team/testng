package org.testng.v6;

import java.util.List;
import java.util.Set;

public class AfterOperationSet {
  private Set<Operation> m_afterOperations = Sets.newHashSet();
  
  public void add(Operation operation) {
    m_afterOperations.add(operation);
  }
  
  public List<Operation> getOperationsThatMustRunAfter(Integer id) {
    List<Operation> result = Lists.newArrayList();
    List<Operation> toRemove = Lists.newArrayList();
    for (Operation o : m_afterOperations) {
      for (int n : o.getAfter()) {
        if (n == id) {
          result.add(o);
          toRemove.add(o);
        }
      }
    }
    
    for (Operation o : toRemove) {
      m_afterOperations.remove(o);
    }
    return result;
  }
}
