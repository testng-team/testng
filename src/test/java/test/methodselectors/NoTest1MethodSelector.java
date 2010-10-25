package test.methodselectors;

import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

import java.util.List;

public class NoTest1MethodSelector implements IMethodSelector {

  /**
   *
   */
  private static final long serialVersionUID = 6706869410370733524L;

  @Override
  public boolean includeMethod(IMethodSelectorContext context,
      ITestNGMethod method, boolean isTestMethod)
  {
    for (String group : method.getGroups()) {
      if (group.equals("test1")) {
        ppp( method.getMethodName() + " is group test1, don't include" );
        context.setStopped(true);
        return false;
      }
    }
    ppp( method.getMethodName() + " is not in group test1" );
    return true;
  }

  @Override
  public void setTestMethods(List<ITestNGMethod> testMethods) {

  }

  public static void ppp(String s) {
    //System.out.println("[NoTest1MethodSelector] " + s);
  }

}
