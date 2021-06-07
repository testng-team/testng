package test.methodselectors;

import java.util.List;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

public class Test2MethodSelector implements IMethodSelector {

  @Override
  public boolean includeMethod(
      IMethodSelectorContext context, ITestNGMethod method, boolean isTestMethod) {
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
