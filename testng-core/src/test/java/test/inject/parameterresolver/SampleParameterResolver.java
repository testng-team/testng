package test.inject.parameterresolver;

import java.lang.reflect.Parameter;
import org.testng.IParameterResolver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/** Owns every {@link FromResolver} annotated parameter and answers a fresh {@link CustomObject}. */
public class SampleParameterResolver implements IParameterResolver {

  @Override
  public boolean supportsParameter(
      Parameter parameter, ITestNGMethod method, ITestContext context) {
    return parameter.isAnnotationPresent(FromResolver.class)
        && CustomObject.class.equals(parameter.getType());
  }

  @Override
  public Object resolveParameter(Parameter parameter, ITestNGMethod method, ITestContext context) {
    return new CustomObject("resolved-" + parameter.getName());
  }
}
