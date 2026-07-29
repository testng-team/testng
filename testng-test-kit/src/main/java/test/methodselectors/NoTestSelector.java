package test.methodselectors;

import java.util.List;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

public class NoTestSelector implements IMethodSelector {

  @Override
  public boolean includeMethod(
      IMethodSelectorContext context, ITestNGMethod method, boolean isTestMethod) {
    return false;
  }

  @Override
  public void setTestMethods(List<ITestNGMethod> testMethods) {}
}
