package test_result.issue2535;

import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import test_result.issue2535.CalculatorTestSample.LocalListener;

@Listeners(LocalListener.class)
public class CalculatorTestSample {

  @Test
  public void calculatorUiTest() {}

  @BeforeClass
  public void setup() {
    Assert.fail();
  }

  @BeforeMethod
  public void setup1() {}

  public static class LocalListener implements IInvokedMethodListener {

    public static final Map<String, ITestResult> results = new HashMap<>();

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
      results.put(method.getTestMethod().getMethodName(), testResult);
    }
  }
}
