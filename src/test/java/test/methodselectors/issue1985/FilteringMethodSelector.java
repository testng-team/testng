package test.methodselectors.issue1985;

import java.util.Arrays;
import java.util.List;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

public class FilteringMethodSelector implements IMethodSelector {

  private static final String ALL = "all";
  public static final String GROUP = "group";
  private String whichGroup;

  public FilteringMethodSelector() {
    whichGroup = System.getProperty(GROUP, ALL);
  }

  @Override
  public boolean includeMethod(
      IMethodSelectorContext context, ITestNGMethod method, boolean isTestMethod) {
    if (ALL.equalsIgnoreCase(whichGroup)) {
      return true;
    }
    boolean isEqual = Arrays.equals(new String[]{whichGroup}, method.getGroups());
    if (context != null) {
      context.setStopped(true);
    }
    return isEqual;
  }

  @Override
  public void setTestMethods(List<ITestNGMethod> testMethods) {
  }
}
