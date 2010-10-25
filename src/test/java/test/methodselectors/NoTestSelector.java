package test.methodselectors;

import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

import java.util.List;

public class NoTestSelector implements IMethodSelector {

  /**
   *
   */
  private static final long serialVersionUID = -7751190578710846487L;

  @Override
  public boolean includeMethod(IMethodSelectorContext context,
      ITestNGMethod method, boolean isTestMethod)
  {
    ppp("NOTEST RETURNING FALSE FOR " + method);
    return false;
  }

  @Override
  public void setTestMethods(List<ITestNGMethod> testMethods) {
  }

  private static void ppp(String s) {
//    System.out.println("[NoTestSelector] " + s);
  }
}
