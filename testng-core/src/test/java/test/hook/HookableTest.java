package test.hook;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.assertj.core.api.SoftAssertions;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.hook.samples.ConfigurableFailureSample;
import test.hook.samples.ConfigurableSuccessSample;
import test.hook.samples.ConfigurableSuccessWithListenerSample;
import test.hook.samples.HookFailureSample;
import test.hook.samples.HookSuccessDynamicParametersSample;
import test.hook.samples.HookSuccessSample;
import test.hook.samples.HookSuccessTimeoutSample;
import test.hook.samples.HookSuccessWithListenerSample;

public class HookableTest extends SimpleBaseTest {

  public static final String HOOK_INVOKED_ATTRIBUTE = "hook";
  public static final String HOOK_METHOD_INVOKED_ATTRIBUTE = "hookMethod";
  public static final String HOOK_METHOD_PARAMS_ATTRIBUTE = "hookParamAttribute";

  @Test(dataProvider = "getTestClasses")
  public void hookSuccess(Class<?> clazz, String flow, boolean assertAttributes) {
    Reporter.log("Running scenario " + flow, true);
    TestNG tng = create(clazz);
    TestResultsCollector listener = new TestResultsCollector();
    tng.addListener(listener);
    tng.run();
    assertThat(listener.getPassedMethodNames()).contains("verify");
    if (!assertAttributes) {
      return;
    }
    SoftAssertions assertions = new SoftAssertions();
    listener
        .getPassed()
        .forEach(
            each -> {
              assertions.assertThat(each.getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNotNull();
              assertions.assertThat(each.getAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE)).isNotNull();
              Object[] parameters = (Object[]) each.getAttribute(HOOK_METHOD_PARAMS_ATTRIBUTE);
              assertions.assertThat(parameters).hasSize(1);
              assertions.assertThat(parameters[0]).isInstanceOf(UUID.class);
            });
    assertions.assertAll();
  }

  @DataProvider(name = "getTestClasses")
  public Object[][] getTestClasses() {
    return new Object[][] {
      {HookSuccessSample.class, "Happy Flow", true},
      {HookSuccessTimeoutSample.class, "With Timeouts (GITHUB-599)", true},
      {HookSuccessDynamicParametersSample.class, "With Dynamic Parameters (GITHUB-862)", false}
    };
  }

  @Test
  public void hookSuccessWithListener() {
    TestNG tng = create(HookSuccessWithListenerSample.class);
    TestResultsCollector listener = new TestResultsCollector();
    tng.addListener(listener);
    tng.run();
    assertThat(listener.getPassedMethodNames()).contains("verify");
    assertThat(listener.getPassed().get(0).getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNotNull();
  }

  @Test
  public void hookFailure() {
    TestNG tng = create(HookFailureSample.class);
    TestResultsCollector listener = new TestResultsCollector();
    tng.addListener(listener);
    tng.run();
    assertThat(listener.getPassedMethodNames()).isNotNull();
    SoftAssertions assertions = new SoftAssertions();
    listener
        .getInvoked()
        .forEach(
            each -> {
              assertions.assertThat(each.getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNotNull();
              assertions.assertThat(each.getAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE)).isNull();
            });
    assertions.assertAll();
  }

  @Test(dataProvider = "getConfigClasses")
  public void configurableSuccess(Class<?> clazz, String flow) {
    Reporter.log("Running scenario " + flow, true);
    TestNG tng = create(clazz);
    TestResultsCollector listener = new TestResultsCollector();
    tng.addListener(listener);
    tng.run();
    assertThat(listener.getPassedConfigs()).hasSize(4);
    assertThat(listener.getPassedConfigNames()).contains("bs", "bt", "bc", "bm");
    SoftAssertions assertions = new SoftAssertions();
    listener
        .getPassedConfigs()
        .forEach(
            each -> {
              assertions.assertThat(each.getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNotNull();
              Object[] parameters = (Object[]) each.getAttribute(HOOK_METHOD_PARAMS_ATTRIBUTE);
              if (parameters != null && parameters.length != 0) {
                assertions.assertThat(parameters).hasSize(1);
                assertions.assertThat(parameters[0]).isInstanceOf(Method.class);
                String methodName = ((Method) parameters[0]).getName();
                assertions.assertThat(methodName).isEqualTo("hookWasRun");
              }
            });
    assertions.assertAll();
  }

  @DataProvider(name = "getConfigClasses")
  public Object[][] getConfigClasses() {
    return new Object[][] {
      {ConfigurableSuccessSample.class, "IConfigurable as test class"},
      {ConfigurableSuccessWithListenerSample.class, "IConfigurable as listener"},
    };
  }

  @Test
  public void configurableFailure() {
    TestNG tng = create(ConfigurableFailureSample.class);
    TestResultsCollector listener = new TestResultsCollector();
    tng.addListener(listener);
    tng.run();
    assertThat(listener.getPassedConfigNames()).containsExactly("bs", "bt", "bc", "bm");
    assertThat(listener.getPassedConfigs()).hasSize(4);
    SoftAssertions assertions = new SoftAssertions();
    listener
        .getPassedConfigs()
        .forEach(
            each -> {
              assertions.assertThat(each.getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNotNull();
              assertions.assertThat(each.getAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE)).isNull();
            });
    assertions.assertAll();
  }

  public static class TestResultsCollector implements IInvokedMethodListener {
    private final List<ITestResult> passed = new ArrayList<>();
    private final List<ITestResult> invoked = new ArrayList<>();
    private final List<ITestResult> passedConfigs = new ArrayList<>();

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
      invoked.add(testResult);
      if (testResult.isSuccess()) {
        if (method.isTestMethod()) {
          passed.add(testResult);
        }
        if (method.isConfigurationMethod()) {
          passedConfigs.add(testResult);
        }
      }
    }

    public List<ITestResult> getPassedConfigs() {
      return passedConfigs;
    }

    public List<ITestResult> getPassed() {
      return passed;
    }

    public List<ITestResult> getInvoked() {
      return invoked;
    }

    public List<String> getPassedMethodNames() {
      return asString(passed);
    }

    public List<String> getPassedConfigNames() {
      return asString(passedConfigs);
    }

    private static List<String> asString(List<ITestResult> list) {
      return list.stream()
          .map(each -> each.getMethod().getMethodName())
          .collect(Collectors.toList());
    }
  }
}
