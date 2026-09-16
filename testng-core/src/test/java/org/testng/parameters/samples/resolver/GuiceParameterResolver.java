package org.testng.parameters.samples.resolver;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import java.lang.reflect.Parameter;
import org.testng.IParameterResolver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/**
 * What a Guice integration would ship: a parameter is owned when the suite's injector has a binding
 * for it. The key is built from {@link Parameter#getParameterizedType()}, so a {@code List<String>}
 * is looked up as such and not as a raw {@code List}; a {@link Named} annotation on the parameter
 * selects the binding of that name.
 *
 * <p>{@code @Inject} cannot be the trigger, since neither Guice's nor Jakarta's targets a
 * parameter. The injector reached here is the suite's parent injector -- what {@link
 * ITestContext#getSuite()} exposes; the per-class one that {@code @Guice(modules = ...)} builds is
 * not reachable through the public API, which is testng-team/testng#3526.
 */
public class GuiceParameterResolver implements IParameterResolver {

  @Override
  public boolean supportsParameter(
      Parameter parameter, ITestNGMethod method, ITestContext context) {
    Injector injector = context.getSuite().getParentInjector();
    return injector != null && injector.getExistingBinding(keyOf(parameter)) != null;
  }

  @Override
  public Object resolveParameter(Parameter parameter, ITestNGMethod method, ITestContext context) {
    Injector injector = context.getSuite().getParentInjector();
    if (injector == null) {
      throw new IllegalStateException("no injector for " + method.getQualifiedName());
    }
    return injector.getInstance(keyOf(parameter));
  }

  private static Key<?> keyOf(Parameter parameter) {
    Named named = parameter.getAnnotation(Named.class);
    return named == null
        ? Key.get(parameter.getParameterizedType())
        : Key.get(parameter.getParameterizedType(), named);
  }
}
