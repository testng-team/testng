package org.testng.internal;

import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * This class contains all the information needed to determine
 * what methods should be run.  It gets invoked by the TestRunner
 * and then goes through its list of method selectors to decide what methods
 * need to be run.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class RunInfo implements Serializable {
  private static final long serialVersionUID = -9085221672822562888L;
  transient private List<MethodSelectorDescriptor>
    m_methodSelectors = Lists.newArrayList();

  public void addMethodSelector(IMethodSelector selector, int priority) {
    Utils.log("RunInfo", 3, "Adding method selector: " + selector + " priority: " + priority);
    MethodSelectorDescriptor md = new MethodSelectorDescriptor(selector, priority);
    m_methodSelectors.add(md);
  }

  /**
   * @return true as soon as we fond a Method Selector that returns
   * true for the method "tm".
   */
  public boolean includeMethod(ITestNGMethod tm, boolean isTestMethod) {
    Collections.sort(m_methodSelectors);
    boolean foundNegative = false;
    IMethodSelectorContext context = new DefaultMethodSelectorContext();

    boolean result = false;
    for (MethodSelectorDescriptor mds : m_methodSelectors) {
      // If we found any negative priority, we break as soon as we encounter
      // a selector with a positive priority
      if (! foundNegative) {
        foundNegative = mds.getPriority() < 0;
      }
      if (foundNegative && mds.getPriority() >= 0) {
        break;
      }

      // Proceeed normally
      IMethodSelector md = mds.getMethodSelector();
      result = md.includeMethod(context, tm, isTestMethod);
      if (context.isStopped()) {
        return result;
      }

      // This selector returned false, move on to the next
    }

    return result;
  }

  public static void ppp(String s) {
    System.out.println("[RunInfo] " + s);
  }

  public void setTestMethods(List<ITestNGMethod> testMethods) {
    for (MethodSelectorDescriptor mds : m_methodSelectors) {
      mds.setTestMethods(testMethods);
    }
  }
}
