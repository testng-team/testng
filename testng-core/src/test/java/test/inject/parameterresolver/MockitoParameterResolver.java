package test.inject.parameterresolver;

import java.lang.reflect.Parameter;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.IParameterResolver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/**
 * What a mocking library would ship: it owns the {@code @Mock} annotated parameters and answers a
 * mock for each, created afresh for every invocation.
 *
 * <p>TestNG depends on nothing of Mockito here; this lives in the tests, and is what gives the SPI
 * a real third party object -- a generated subclass or proxy rather than an exact instance of the
 * declared type -- to place back into an invocation.
 */
public class MockitoParameterResolver implements IParameterResolver {

  @Override
  public boolean supportsParameter(
      Parameter parameter, ITestNGMethod method, ITestContext context) {
    return parameter.isAnnotationPresent(Mock.class);
  }

  @Override
  public Object resolveParameter(Parameter parameter, ITestNGMethod method, ITestContext context) {
    return Mockito.mock(parameter.getType());
  }
}
