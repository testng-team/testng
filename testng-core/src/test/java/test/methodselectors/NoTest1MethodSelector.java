package test.methodselectors;

import java.util.List;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

public class NoTest1MethodSelector implements IMethodSelector {

  @Override
  public boolean includeMethod(
      IMethodSelectorContext context, ITestNGMethod method, boolean isTestMethod) {
    for (String group : method.getGroups()) {
      if (group.equals("test1")) {
        context.setStopped(true);
        return false;
      }
    }
    return true;
  }

  @Override
  public void setTestMethods(List<ITestNGMethod> testMethods) {}
}
