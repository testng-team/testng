package test.methodselectors.issue2595;

import java.util.List;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

public class OnlyDoNothingSelector implements IMethodSelector {

  @Override
  public boolean includeMethod(
      IMethodSelectorContext context, ITestNGMethod method, boolean isTestMethod) {
    return "doNothing".equals(method.getMethodName());
  }

  @Override
  public void setTestMethods(List<ITestNGMethod> testMethods) {}
}
