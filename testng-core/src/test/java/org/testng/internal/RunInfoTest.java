package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class RunInfoTest {

  @Test(description = "GITHUB-2595")
  public void equalPrioritySelectorsAreInvokedInInsertionOrder() {
    List<String> invoked = new ArrayList<>();
    IMethodSelector first = recordingSelector("first", invoked);
    IMethodSelector second = recordingSelector("second", invoked);
    RunInfo runInfo = new RunInfo(() -> new XmlTest(new XmlSuite()));
    runInfo.addMethodSelector(first, 10);
    runInfo.addMethodSelector(second, 10);

    boolean included = runInfo.includeMethod(mock(ITestNGMethod.class), true);

    assertThat(invoked).containsExactly("first", "second");
    assertThat(included).isTrue();
  }

  private static IMethodSelector recordingSelector(String name, List<String> invoked) {
    return new IMethodSelector() {
      @Override
      public boolean includeMethod(
          IMethodSelectorContext context, ITestNGMethod method, boolean isTestMethod) {
        invoked.add(name);
        return true;
      }

      @Override
      public void setTestMethods(List<ITestNGMethod> testMethods) {}
    };
  }
}
