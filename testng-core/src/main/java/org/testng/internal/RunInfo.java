package org.testng.internal;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;
import org.testng.xml.XmlTest;

/**
 * This class contains all the information needed to determine what methods should be run. It gets
 * invoked by the TestRunner and then goes through its list of method selectors to decide what
 * methods need to be run.
 */
public class RunInfo {

  private final Set<MethodSelectorDescriptor> m_methodSelectors = new TreeSet<>();
  private final Supplier<XmlTest> xmlTest;

  public RunInfo(Supplier<XmlTest> xmlTest) {
    this.xmlTest = xmlTest;
  }

  public XmlTest getXmlTest() {
    return xmlTest.get();
  }

  public void addMethodSelector(IMethodSelector selector, int priority) {
    Utils.log("RunInfo", 3, "Adding method selector: " + selector + " priority: " + priority);
    MethodSelectorDescriptor md = new MethodSelectorDescriptor(selector, priority);
    m_methodSelectors.add(md);
  }

  /** @return true as soon as we fond a Method Selector that returns true for the method "tm". */
  public boolean includeMethod(ITestNGMethod tm, boolean isTestMethod) {
    boolean foundNegative = false;
    IMethodSelectorContext context = new DefaultMethodSelectorContext();

    boolean result = false;
    for (MethodSelectorDescriptor mds : m_methodSelectors) {
      // If we found any negative priority, we break as soon as we encounter
      // a selector with a positive priority
      if (!foundNegative) {
        foundNegative = mds.getPriority() < 0;
      }
      if (foundNegative && mds.getPriority() >= 0) {
        break;
      }

      // Proceed normally
      IMethodSelector md = mds.getMethodSelector();
      result = md.includeMethod(context, tm, isTestMethod);
      if (context.isStopped()) {
        return result;
      }

      // This selector returned false, move on to the next
    }

    return result;
  }

  public void setTestMethods(List<ITestNGMethod> testMethods) {
    for (MethodSelectorDescriptor mds : m_methodSelectors) {
      mds.setTestMethods(testMethods);
    }
  }
}
