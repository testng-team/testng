package test.methodselectors;

import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

import java.util.List;

public class Test2MethodSelector implements IMethodSelector {

  /**
   *
   */
  private static final long serialVersionUID = 4166247968392649912L;

  @Override
  public boolean includeMethod(IMethodSelectorContext context,
      ITestNGMethod method, boolean isTestMethod)
  {
    for (String group : method.getGroups()) {
      if (group.equals("test2")) {
        context.setStopped(true);
        return true;
      }
    }

    return false;
  }

  @Override
  public void setTestMethods(List<ITestNGMethod> testMethods) {
    // TODO Auto-generated method stub

  }

}
