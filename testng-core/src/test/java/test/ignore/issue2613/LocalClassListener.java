package test.ignore.issue2613;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.IClassListener;
import org.testng.ITestClass;

public class LocalClassListener implements IClassListener {

  private List<String> methods;

  @Override
  public void onBeforeClass(ITestClass testClass) {
    methods =
        Arrays.stream(testClass.getTestMethods())
            .map(each -> each.getConstructorOrMethod().getMethod().getName())
            .collect(Collectors.toList());
  }

  public List<String> getMethods() {
    return methods;
  }
}
