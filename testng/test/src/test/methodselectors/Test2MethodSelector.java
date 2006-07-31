package test.methodselectors;

import java.util.List;

import org.testng.IMethodSelector;
import org.testng.ITestNGMethod;

public class Test2MethodSelector implements IMethodSelector {

  public boolean includeMethod(ITestNGMethod method, boolean isTestMethod) {
    for (String group : method.getGroups()) {
      if (group.equals("test2")) {
        return true;
      }
    }
    
    return false;
  }

  public void setTestMethods(List<ITestNGMethod> testMethods) {
    // TODO Auto-generated method stub
    
  }

}
