package test.methodselectors;

import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

import java.util.List;

public class AllTestsMethodSelector implements IMethodSelector {

  /**
   *
   */
  private static final long serialVersionUID = 8059117082807260868L;

  @Override
  public boolean includeMethod(IMethodSelectorContext context,
      ITestNGMethod method, boolean isTestMethod)
  {
    context.setStopped(true);
    return true;
  }

  private static void ppp(String s) {
    System.out.println("[MyMethodSelector] " + s);
  }

  @Override
  public void setTestMethods(List<ITestNGMethod> testMethods) {
    // TODO Auto-generated method stub

  }

}
