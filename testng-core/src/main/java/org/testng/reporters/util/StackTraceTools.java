package org.testng.reporters.util;

import org.testng.ITestNGMethod;

/**
 * Functionality to allow tools to analyse and subdivide stack traces.
 *
 * @author Paul Mendelson
 * @since 5.3
 * @version $Revision: 173 $
 */
public final class StackTraceTools {
  private StackTraceTools() {
    // defeat instantiation.
  }

  /**
   * @param stack The stack trace
   * @param method The test method
   * @return topmost position of the test method in the stack, or top of stack if <code>method
   *     </code> is not in it.
   */
  public static int getTestRoot(StackTraceElement[] stack, ITestNGMethod method) {
    if (stack == null || method == null) {
      return -1;
    }
    String cname = method.getTestClass().getName();
    for (int x = stack.length - 1; x >= 0; x--) {
      if (cname.equals(stack[x].getClassName())
          && method.getMethodName().equals(stack[x].getMethodName())) {
        return x;
      }
    }
    return stack.length - 1;
  }

  /**
   * @param stack The stacktrace
   * @param method The test method
   * @return topmost position of the test method in the stack, or top of stack if <code>method
   *     </code> is not in it.
   */
  public static StackTraceElement[] getTestNGInstrastructure(
      StackTraceElement[] stack, ITestNGMethod method) {
    if (method == null || stack == null) {
      return new StackTraceElement[] {};
    }
    int slot = StackTraceTools.getTestRoot(stack, method);
    if (slot >= 0) {
      StackTraceElement[] r = new StackTraceElement[stack.length - slot];
      System.arraycopy(stack, slot, r, 0, r.length);
      return r;
    } else {
      return new StackTraceElement[0];
    }
  }
}
