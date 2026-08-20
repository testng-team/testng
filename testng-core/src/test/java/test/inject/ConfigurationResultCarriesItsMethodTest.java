package test.inject;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * The {@link ITestResult} a configuration method is handed is the parameter carrier the invoker
 * builds before it knows the outcome of the invocation. That carrier used to be built without a
 * method and be given one moments later, so it answered null for its name and its instance name for
 * the whole of the configuration method's life.
 *
 * <p>The attributes travel: the carrier is replaced by the reported result, which copies them.
 */
public class ConfigurationResultCarriesItsMethodTest {

  @BeforeMethod
  public void setUp(ITestResult carrier) {
    carrier.setAttribute("methodName", carrier.getMethod().getMethodName());
    carrier.setAttribute("name", carrier.getName());
    carrier.setAttribute("instanceName", carrier.getInstanceName());
  }

  @Test
  public void theCarrierNamesTheMethodItReports() {
    ITestResult result = Reporter.getCurrentTestResult();

    assertThat(result.getAttribute("methodName")).isEqualTo("theCarrierNamesTheMethodItReports");
    assertThat(result.getAttribute("name")).isEqualTo("theCarrierNamesTheMethodItReports");
    assertThat(result.getAttribute("instanceName")).isEqualTo(getClass().getName());
  }
}
