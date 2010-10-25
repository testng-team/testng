package org.testng.reporters.util;

import org.testng.ITestNGMethod;

/**
 * Functionality to allow tools to analyse and subdivide stack traces.
 *
 * @author Paul Mendelson
 * @since 5.3
 * @version $Revision: 173 $
 */
public class StackTraceTools {
  // ~ Methods --------------------------------------------------------------

  /** Finds topmost position of the test method in the stack, or top of stack if <code>method</code> is not in it. */
  public static int getTestRoot(StackTraceElement[] stack,ITestNGMethod method) {
    if(stack!=null) {
      String cname = method.getTestClass().getName();
      for(int x=stack.length-1; x>=0; x--) {
        if(cname.equals(stack[x].getClassName())
            && method.getMethodName().equals(stack[x].getMethodName())) {
          return x;
        }
      }
      return stack.length-1;
    } else {
      return -1;
    }
  }

  /** Finds topmost position of the test method in the stack, or top of stack if <code>method</code> is not in it. */
  public static StackTraceElement[] getTestNGInstrastructure(StackTraceElement[] stack,ITestNGMethod method) {
    int slot=StackTraceTools.getTestRoot(stack, method);
    if(slot>=0) {
      StackTraceElement[] r=new StackTraceElement[stack.length-slot];
      for(int x=0; x<r.length; x++) {
        r[x]=stack[x+slot];
      }
      return r;
    } else {
      return new StackTraceElement[0];
    }
  }
}
