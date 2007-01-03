package test.methodselectors;

import java.util.List;

import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

public class NoTestSelector implements IMethodSelector {

  public boolean includeMethod(IMethodSelectorContext context,
      ITestNGMethod method, boolean isTestMethod) 
  {
    ppp("NOTEST RETURNING FALSE FOR " + method);
    return false;
  }

  public void setTestMethods(List<ITestNGMethod> testMethods) {
  }

  private static void ppp(String s) {
    System.out.println("[NoTestSelector] " + s);
  }
}
