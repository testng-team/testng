package org.testng.internal.reflect;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.testng.IParameterResolver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.internal.Utils;

/**
 * The parameters of one test method that an {@link IParameterResolver} owns, and nothing else: they
 * are the second reason -- beside {@link InjectableParameter}, TestNG's own closed set -- for a
 * parameter not to consume a data provider value.
 *
 * <p>Ownership is decided once for the whole set of matchers that build one invocation, so a
 * resolver is asked {@link IParameterResolver#supportsParameter} the same number of times whichever
 * matcher ends up conforming -- but once per invocation, not once per method, since nothing here
 * outlives the arguments it was built for. {@link #resolve} runs the owning resolver, once per
 * parameter per invocation.
 */
public final class ResolvedParameters {

  private static final ResolvedParameters NONE = new ResolvedParameters(Collections.emptyMap());

  private final Map<Parameter, Ownership> owners;

  private ResolvedParameters(Map<Parameter, Ownership> owners) {
    this.owners = owners;
  }

  /** No parameter is externally resolved, which is every run that registers no resolver. */
  public static ResolvedParameters none() {
    return NONE;
  }

  /**
   * Asks each resolver which parameters it owns.
   *
   * <p>A parameter TestNG injects natively is never offered, so a resolver cannot displace one. A
   * parameter carrying {@link org.testng.annotations.NoInjection} is not natively injected and is
   * therefore offered.
   *
   * @throws TestNGException if more than one resolver claims the same parameter, or if a resolver
   *     throws while deciding.
   */
  public static ResolvedParameters of(
      Parameter[] parameters,
      ITestNGMethod method,
      ITestContext context,
      Collection<IParameterResolver> resolvers) {
    if (parameters.length == 0 || resolvers.isEmpty()) {
      return NONE;
    }
    // Both collections stay null until something is actually claimed, which is the common case even
    // once a resolver is registered: most parameters still come from the data provider.
    Map<Parameter, Ownership> owners = null;
    for (int index = 0; index < parameters.length; index++) {
      Parameter parameter = parameters[index];
      if (ReflectionRecipes.isNativelyInjectable(parameter)) {
        continue;
      }
      IParameterResolver owner = null;
      List<IParameterResolver> competing = null;
      for (IParameterResolver resolver : resolvers) {
        if (!resolver.isEnabled() || !supports(resolver, parameter, index, method, context)) {
          continue;
        }
        if (owner == null) {
          owner = resolver;
        } else {
          if (competing == null) {
            competing = new ArrayList<>(2);
            competing.add(owner);
          }
          competing.add(resolver);
        }
      }
      if (competing != null) {
        throw new TestNGException(
            "More than one "
                + IParameterResolver.class.getSimpleName()
                + " claims "
                + describe(parameter, index)
                + " of "
                + method.getQualifiedName()
                + ": "
                + competing.stream()
                    .map(resolver -> resolver.getClass().getName())
                    .collect(Collectors.joining(", "))
                + ". A parameter can be owned by only one resolver.");
      }
      if (owner != null) {
        if (owners == null) {
          owners = new LinkedHashMap<>();
        }
        owners.put(parameter, new Ownership(owner, index, method, context));
      }
    }
    return owners == null ? NONE : new ResolvedParameters(owners);
  }

  private static boolean supports(
      IParameterResolver resolver,
      Parameter parameter,
      int index,
      ITestNGMethod method,
      ITestContext context) {
    try {
      return resolver.supportsParameter(parameter, method, context);
    } catch (RuntimeException | Error cause) {
      throw new TestNGException(
          resolver.getClass().getName()
              + ".supportsParameter() failed for "
              + describe(parameter, index)
              + " of "
              + method.getQualifiedName(),
          cause);
    }
  }

  public boolean isEmpty() {
    return owners.isEmpty();
  }

  /** Whether a resolver supplies this parameter, rather than the data provider. */
  public boolean owns(Parameter parameter) {
    return owners.containsKey(parameter);
  }

  /**
   * Runs the owning resolver and checks what it answered against the parameter type.
   *
   * @throws TestNGException if the resolver throws, or answers a value the parameter cannot take.
   */
  public @Nullable Object resolve(Parameter parameter) {
    Ownership ownership = owners.get(parameter);
    if (ownership == null) {
      throw new TestNGException("No resolver owns " + parameter + "; it should not be resolved");
    }
    Object value;
    try {
      value = ownership.resolver.resolveParameter(parameter, ownership.method, ownership.context);
    } catch (RuntimeException | Error cause) {
      throw new TestNGException(
          ownership.resolver.getClass().getName()
              + ".resolveParameter() failed for "
              + describe(parameter, ownership.index)
              + " of "
              + ownership.method.getQualifiedName(),
          cause);
    }
    if (!ReflectionRecipes.isInstanceOf(parameter.getType(), value)) {
      throw new TestNGException(
          ownership.resolver.getClass().getName()
              + " resolved "
              + describe(parameter, ownership.index)
              + " of "
              + ownership.method.getQualifiedName()
              + " to "
              + describeValue(value)
              + ", which is not assignable to "
              + parameter.getType().getName());
    }
    return value;
  }

  /**
   * Naming the value that was refused means running the resolver's {@code toString()}, from inside
   * a path that is already reporting a failure. {@link Utils#toString} is the failsafe rendering
   * TestNG added for GITHUB-2830, and it also spells arrays out rather than as an identity hash.
   */
  private static String describeValue(@Nullable Object value) {
    if (value == null) {
      return "null";
    }
    return value.getClass().getName() + " (" + Utils.toString(value) + ")";
  }

  private static String describe(Parameter parameter, int index) {
    return "parameter "
        + index
        + " ["
        + parameter.getType().getName()
        + " "
        + parameter.getName()
        + "]";
  }

  /** Who supplies one parameter, and everything answering that needs. */
  private static final class Ownership {
    private final IParameterResolver resolver;
    private final int index;
    private final ITestNGMethod method;
    private final ITestContext context;

    private Ownership(
        IParameterResolver resolver, int index, ITestNGMethod method, ITestContext context) {
      this.resolver = resolver;
      this.index = index;
      this.method = method;
      this.context = context;
    }
  }
}
