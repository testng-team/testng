package org.testng.internal;

import java.util.Comparator;

import org.testng.ITestNGMethod;

public class TestMethodComparator implements Comparator<ITestNGMethod> {

  @Override
  public int compare(ITestNGMethod o1, ITestNGMethod o2) {
    return compareStatic(o1, o2);
  }

  public static int compareStatic(ITestNGMethod o1, ITestNGMethod o2) {
    int prePriDiff = o1.getInterceptedPriority() - o2.getInterceptedPriority();
    if (prePriDiff != 0) {
      return prePriDiff;
    }

    int priDiff = o1.getPriority() - o2.getPriority();
    if (priDiff != 0) {
      return priDiff;
    }

    return o1.getMethodName().compareTo(o2.getMethodName());
  }
}
