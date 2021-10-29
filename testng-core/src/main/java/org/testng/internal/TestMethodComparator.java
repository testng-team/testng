package org.testng.internal;

import java.util.Comparator;
import org.testng.ITestNGMethod;

public class TestMethodComparator implements Comparator<ITestNGMethod> {

  @Override
  public int compare(ITestNGMethod o1, ITestNGMethod o2) {
    return compareStatic(o1, o2);
  }

  public static int compareStatic(ITestNGMethod o1, ITestNGMethod o2) {
    int interceptedPriorityDiff =
        Integer.compare(o1.getInterceptedPriority(), o2.getInterceptedPriority());
    if (interceptedPriorityDiff != 0) {
      return interceptedPriorityDiff;
    }

    int priorityDiff = Integer.compare(o1.getPriority(), o2.getPriority());
    if (priorityDiff != 0) {
      return priorityDiff;
    }

    int classHierarchyPriorityDiff =
        Integer.compare(o1.getClassHierarchyPriority(), o2.getClassHierarchyPriority());
    if (classHierarchyPriorityDiff != 0) {
      return classHierarchyPriorityDiff;
    }

    return o1.getMethodName().compareTo(o2.getMethodName());
  }
}
