package test.methodselectors;

import java.util.List;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

public class NoTest1MethodSelector implements IMethodSelector {

  public static void ppp(String s) {
    //System.out.println("[NoTest1MethodSelector] " + s);
  }

  @Override
  public boolean includeMethod(IMethodSelectorContext context,
      ITestNGMethod method, boolean isTestMethod) {
    for (String group : method.getGroups()) {
      if (group.equals("test1")) {
        ppp(method.getMethodName() + " is group test1, don't include");
        context.setStopped(true);
        return false;
      }
    }
    ppp(method.getMethodName() + " is not in group test1");
    return true;
  }

  @Override
  public void setTestMethods(List<ITestNGMethod> testMethods) {

  }

}
