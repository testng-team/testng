package org.testng;

import java.util.Collections;
import java.util.List;

import org.testng.internal.MethodInstance;

/**
 * A method interceptor that preserves the order in which test classes were found in
 * the &lt;test&gt; tag.
 * 
 * @author cbeust
 *
 */
public class PreserveOrderMethodInterceptor implements IMethodInterceptor {

  private void p(List<IMethodInstance> methods, String s) {
    System.out.println("[PreserveOrderMethodInterceptor] " + s);
    for (IMethodInstance mi : methods) {
      System.out.println("  " + mi.getMethod().getMethodName()
          + " index:" + mi.getMethod().getTestClass().getXmlClass().getIndex());
    }
  }

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
//    p(methods, "Before");
    Collections.sort(methods, MethodInstance.SORT_BY_INDEX);
//    p(methods, "After");
    return methods;
  }

}
