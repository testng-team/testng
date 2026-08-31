package test.inject.parameterresolver;

import java.lang.reflect.Parameter;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.testng.IParameterResolver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/**
 * A resolver whose two decisions are supplied by the test, so that one shape covers the failing
 * cases -- a value of the wrong type, a resolver that throws -- without a class per case.
 */
public class ConfigurableParameterResolver implements IParameterResolver {

  private final Function<Parameter, Boolean> supports;
  private final Function<Parameter, @Nullable Object> resolve;

  public ConfigurableParameterResolver(
      Function<Parameter, Boolean> supports, Function<Parameter, @Nullable Object> resolve) {
    this.supports = supports;
    this.resolve = resolve;
  }

  /** Claims every {@link FromResolver} annotated parameter and answers what the test decided. */
  public static ConfigurableParameterResolver answering(
      Function<Parameter, @Nullable Object> resolve) {
    return new ConfigurableParameterResolver(
        parameter -> parameter.isAnnotationPresent(FromResolver.class), resolve);
  }

  @Override
  public boolean supportsParameter(
      Parameter parameter, ITestNGMethod method, ITestContext context) {
    return supports.apply(parameter);
  }

  @Override
  public @Nullable Object resolveParameter(
      Parameter parameter, ITestNGMethod method, ITestContext context) {
    return resolve.apply(parameter);
  }
}
