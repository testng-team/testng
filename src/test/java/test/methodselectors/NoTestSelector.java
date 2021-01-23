package test.methodselectors;

import java.util.List;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

public class NoTestSelector implements IMethodSelector {

  private static void ppp(String s) {
//    System.out.println("[NoTestSelector] " + s);
  }

  @Override
  public boolean includeMethod(IMethodSelectorContext context,
      ITestNGMethod method, boolean isTestMethod) {
    ppp("NOTEST RETURNING FALSE FOR " + method);
    return false;
  }

  @Override
  public void setTestMethods(List<ITestNGMethod> testMethods) {
  }
}
