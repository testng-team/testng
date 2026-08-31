package test.inject.parameterresolver;

import java.lang.reflect.Parameter;
import org.testng.IParameterResolver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/** Claims exactly what {@link SampleParameterResolver} claims, so the two must not both win. */
public class CompetingParameterResolver implements IParameterResolver {

  @Override
  public boolean supportsParameter(
      Parameter parameter, ITestNGMethod method, ITestContext context) {
    return parameter.isAnnotationPresent(FromResolver.class);
  }

  @Override
  public Object resolveParameter(Parameter parameter, ITestNGMethod method, ITestContext context) {
    return new CustomObject("competing");
  }
}
