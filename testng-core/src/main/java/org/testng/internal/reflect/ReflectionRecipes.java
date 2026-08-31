package org.testng.internal.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestNGException;
import org.testng.annotations.NoInjection;
import org.testng.internal.RuntimeBehavior;
import org.testng.xml.XmlTest;

/**
 * Utility class to handle reflection.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public final class ReflectionRecipes {

  private static final Map<Class<?>, Class<?>> PRIMITIVE_MAPPING = new HashMap<>();

  /**
   * Boxed types that widen to each primitive. Keyed by exactly the primitives of {@link
   * #PRIMITIVE_MAPPING}, so a lookup never returns null; one with no widening source maps to an
   * empty list.
   */
  private static final Map<Class<?>, List<Class<?>>> ASSIGNABLE_MAPPING = new HashMap<>();

  static {
    initPrimitiveMapping();
    initAssignableMapping();
    // Primitives nothing widens to are left out above; give them an entry so the two tables share
    // one key set and callers never have to guard the lookup.
    for (final Class<?> primitive : PRIMITIVE_MAPPING.keySet()) {
      ASSIGNABLE_MAPPING.putIfAbsent(primitive, Collections.emptyList());
    }
  }

  private static void initPrimitiveMapping() {
    PRIMITIVE_MAPPING.put(boolean.class, Boolean.class);
    PRIMITIVE_MAPPING.put(byte.class, Byte.class);
    PRIMITIVE_MAPPING.put(short.class, Short.class);
    PRIMITIVE_MAPPING.put(int.class, Integer.class);
    PRIMITIVE_MAPPING.put(long.class, Long.class);
    PRIMITIVE_MAPPING.put(float.class, Float.class);
    PRIMITIVE_MAPPING.put(double.class, Double.class);
    PRIMITIVE_MAPPING.put(char.class, Character.class);
    PRIMITIVE_MAPPING.put(void.class, Void.class);
  }

  private static void initAssignableMapping() {
    ASSIGNABLE_MAPPING.put(
        double.class,
        Arrays.asList(
            Float.class, Long.class, Integer.class, Short.class, Character.class, Byte.class));
    ASSIGNABLE_MAPPING.put(
        float.class,
        Arrays.asList(Long.class, Integer.class, Short.class, Character.class, Byte.class));
    ASSIGNABLE_MAPPING.put(
        long.class, Arrays.asList(Integer.class, Short.class, Character.class, Byte.class));
    ASSIGNABLE_MAPPING.put(int.class, Arrays.asList(Short.class, Character.class, Byte.class));
    ASSIGNABLE_MAPPING.put(short.class, Arrays.asList(Byte.class));
  }

  private ReflectionRecipes() {
    throw new TestNGException("Service is not meant to have instances");
  }

  /**
   * Checks if an instance is an instance of the given class.
   *
   * @param reference reference class.
   * @param object instance to be tested. A null one is an instance of any reference type and of no
   *     primitive one, which is what a data provider supplying a null value already relied on.
   * @return is an instance-of or not
   */
  public static boolean isInstanceOf(final Class<?> reference, final @Nullable Object object) {
    if (object == null) {
      return !reference.isPrimitive();
    }
    boolean isInstanceOf;
    final boolean directInstance = reference.isInstance(object);
    if (!directInstance && reference.isPrimitive()) {
      // Both tables share one key set (see the static initialiser) and isPrimitive() was just
      // checked, so neither lookup misses.
      isInstanceOf = Objects.requireNonNull(PRIMITIVE_MAPPING.get(reference)).isInstance(object);
      if (!isInstanceOf) {
        isInstanceOf =
            Objects.requireNonNull(ASSIGNABLE_MAPPING.get(reference)).contains(object.getClass());
      }

    } else {
      isInstanceOf = directInstance;
    }
    return isInstanceOf;
  }

  /**
   * Checks a class instance for being a given interface or its implementation.
   *
   * @param reference reference interface instance.
   * @param clazz class instance to be tested.
   * @return would an instance of 'clazz' be an instance of reference interface.
   */
  public static boolean isOrImplementsInterface(final Class<?> reference, final Class<?> clazz) {
    boolean implementsInterface = false;
    if (reference.isInterface()) {
      if (reference.equals(clazz)) {
        implementsInterface = true;
      } else {
        final Class<?>[] interfaces = clazz.getInterfaces();
        for (final Class<?> interfaceClazz : interfaces) {
          implementsInterface = interfaceClazz.equals(reference);
          if (implementsInterface) {
            break;
          }
        }
      }
    }
    return implementsInterface;
  }

  /**
   * Checks a class instance for being a given class or its sub-class.
   *
   * @param reference reference class instance.
   * @param clazz class instance to be tested.
   * @return would an instance of 'clazz' be an instance of reference class.
   */
  public static boolean isOrExtends(final Class<?> reference, final Class<?> clazz) {
    boolean extendsGiven = false;
    if (clazz != null) {
      if (!reference.isInterface()) {
        if (reference.equals(clazz)) {
          extendsGiven = true;
        } else {
          extendsGiven = isOrExtends(reference, clazz.getSuperclass());
        }
      }
    }
    return extendsGiven;
  }

  /**
   * Extracts class instances from parameters.
   *
   * @param parameters an array of parameters.
   * @return parameter types.
   */
  public static Class<?>[] classesFromParameters(final Parameter[] parameters) {
    final Class<?>[] classes = new Class<?>[parameters.length];
    int i = 0;
    for (final Parameter parameter : parameters) {
      classes[i] = parameter.getType();
      i++;
    }
    return classes;
  }

  /**
   * Extracts method parameters.
   *
   * @param method any valid method.
   * @return extracted method parameters.
   */
  public static Parameter[] getMethodParameters(final @Nullable Method method) {
    if (method == null) {
      return new Parameter[] {};
    }
    return method.getParameters();
  }

  /**
   * Extracts constructor parameters.
   *
   * @param constructor any valid constructor.
   * @return extracted constructor parameters.
   */
  public static Parameter[] getConstructorParameters(final Constructor<?> constructor) {
    if (constructor == null) {
      return new Parameter[] {};
    }
    return constructor.getParameters();
  }

  /**
   * @return matches or not
   * @see #matchArrayEnding(Class[], Object[])
   */
  public static boolean matchArrayEnding(final Parameter[] parameters, final Object[] param) {
    return matchArrayEnding(classesFromParameters(parameters), param);
  }

  /**
   * Matches an array of class instances to an array of instances having last class instance an
   * array.
   *
   * <p>Assuming upper case letters denote classes and corresponding lowercase its instances.
   * Classes {A,B,C...}, instances {a,b,c1,c2} ==&gt; check for {a,b,{c1,c2}} match or Classes
   * {A,B,C[]}, instances {a,b,c1,c2} ==&gt; check for {a,b,{c1,c2}} match both of the above cases
   * are equivalent.
   *
   * @param classes array of class instances to check against.
   * @param args instances to be verified.
   * @return matches or not
   */
  public static boolean matchArrayEnding(final Class<?>[] classes, final Object[] args) {
    if (classes.length < 1) {
      return false;
    }
    if (!classes[classes.length - 1].isArray()) {
      return false;
    }
    boolean matching = true;
    int i = 0;
    if (classes.length <= args.length) {
      for (final Class<?> clazz : classes) {
        if (i >= classes.length - 1) {
          break;
        }
        matching = ReflectionRecipes.isInstanceOf(clazz, args[i]);
        i++;
        if (!matching) {
          break;
        }
      }
    } else {
      matching = false;
    }

    if (matching) {
      final Class<?> componentType = classes[classes.length - 1].getComponentType();
      for (; i < args.length; i++) {
        matching = ReflectionRecipes.isInstanceOf(componentType, args[i]);
        if (!matching) {
          break;
        }
      }
    }

    return matching;
  }

  /**
   * Matches an array of parameters to an array of instances.
   *
   * @return matches or not
   * @see #exactMatch(Class[], Object[])
   */
  public static boolean exactMatch(final Parameter[] parameters, final Object[] args) {
    return exactMatch(classesFromParameters(parameters), args);
  }

  /**
   * Matches an array of class instances to an array of instances.
   *
   * @param classes array of class instances to check against.
   * @param args instances to be verified.
   * @return matches or not
   */
  public static boolean exactMatch(final Class<?>[] classes, final Object[] args) {
    boolean matching = true;
    if (classes.length == args.length) {
      int i = 0;
      for (final Class<?> clazz : classes) {
        matching = ReflectionRecipes.isInstanceOf(clazz, args[i]);
        i++;
        if (!matching) {
          break;
        }
      }
    } else {
      matching = false;
    }
    return matching;
  }

  /**
   * Omits 1. org.testng.ITestContext or its implementations from input array 2.
   * org.testng.ITestResult or its implementations from input array 3. org.testng.xml.XmlTest or its
   * implementations from input array 4. First method depending on filters.
   *
   * <p>An example would be Input: {ITestContext.class, int.class, Boolean.class, TestContext.class}
   * Output: {int.class, Boolean.class}
   *
   * @param parameters array of parameter instances under question.
   * @param filters filters to use.
   * @return Injects free array of class instances.
   */
  public static Parameter[] filter(
      final Parameter[] parameters, final Set<InjectableParameter> filters) {
    return filter(parameters, filters, ResolvedParameters.none());
  }

  /**
   * The same, also omitting the parameters an {@link org.testng.IParameterResolver} owns: they are
   * supplied by the resolver, so they must not be matched against the user supplied arguments
   * either.
   *
   * @param parameters array of parameter instances under question.
   * @param filters filters to use.
   * @param resolved the parameters supplied by a resolver rather than by the caller.
   * @return Injects free array of class instances.
   */
  public static Parameter[] filter(
      final Parameter[] parameters,
      final Set<InjectableParameter> filters,
      final ResolvedParameters resolved) {
    // Both entry points have always tolerated a null filter set. The guard used to be the only
    // thing standing between that null and the loop; a resolver is now reason enough to walk the
    // parameters, so normalise once here instead.
    final Set<InjectableParameter> injects =
        Objects.requireNonNullElse(filters, InjectableParameter.Assistant.NONE);
    if (injects.isEmpty() && resolved.isEmpty()) {
      return parameters;
    }
    boolean firstMethodFiltered = false;
    final List<Parameter> filterList = new ArrayList<>(parameters.length);
    for (final Parameter parameter : parameters) {
      if (resolved.owns(parameter)) {
        continue;
      }
      boolean omit = false;
      for (final InjectableParameter injectableParameter : injects) {
        omit = canInject(parameter, injectableParameter);
        if (injectableParameter == InjectableParameter.CURRENT_TEST_METHOD) {
          if (omit && !firstMethodFiltered) {
            firstMethodFiltered = true;
          } else {
            omit = false;
          }
        }
        if (omit) {
          break;
        }
      }
      if (!omit) {
        filterList.add(parameter);
      }
    }
    return filterList.toArray(new Parameter[0]);
  }

  /**
   * Injects appropriate arguments.
   *
   * @param parameters array of parameter instances under question.
   * @param filters filters to use.
   * @param args user supplied arguments.
   * @param injectionMethod current test method, or {@code null} when there is no holder.
   * @param context current test context.
   * @param testResult on going test results.
   * @return injected arguments.
   */
  public static Object[] inject(
      final Parameter[] parameters,
      final Set<InjectableParameter> filters,
      final Object[] args,
      final @Nullable Method injectionMethod,
      final @Nullable ITestContext context,
      final @Nullable ITestResult testResult) {
    return nativelyInject(
        parameters, filters, ResolvedParameters.none(), args, injectionMethod, context, testResult);
  }

  /**
   * The same, also placing back the values an {@link org.testng.IParameterResolver} supplies. A
   * natively injectable parameter is injected first, so a resolver never displaces one.
   *
   * @param parameters array of parameter instances under question.
   * @param filters filters to use.
   * @param resolved the parameters supplied by a resolver rather than by the caller.
   * @param args user supplied arguments.
   * @param injectionMethod current test method, or {@code null} when there is no holder.
   * @param context current test context.
   * @param testResult on going test results.
   * @return injected arguments.
   */
  public static Object[] inject(
      final Parameter[] parameters,
      final Set<InjectableParameter> filters,
      final ResolvedParameters resolved,
      final Object[] args,
      final @Nullable Method injectionMethod,
      final @Nullable ITestContext context,
      final @Nullable ITestResult testResult) {
    return nativelyInject(
        parameters, filters, resolved, args, injectionMethod, context, testResult);
  }

  private static Object[] nativelyInject(
      final Parameter[] parameters,
      final Set<InjectableParameter> filters,
      final ResolvedParameters resolved,
      final Object[] args,
      final @Nullable Object injectionMethod,
      final @Nullable ITestContext context,
      final @Nullable ITestResult testResult) {
    final Set<InjectableParameter> injects =
        Objects.requireNonNullElse(filters, InjectableParameter.Assistant.NONE);
    if (injects.isEmpty() && resolved.isEmpty()) {
      return args;
    }
    final ArrayList<Object> arguments = new ArrayList<>(args.length);
    final ListBackedImmutableQueue<Object> queue = new ListBackedImmutableQueue<>(args);
    boolean firstMethodInjected = false;
    for (final Parameter parameter : parameters) {
      boolean inject = false;
      Object injectObject = null;
      for (final InjectableParameter injectableParameter : injects) {
        inject = canInject(parameter, injectableParameter);
        if (inject) {
          switch (injectableParameter) {
            case CURRENT_TEST_METHOD:
              if (!firstMethodInjected) {
                firstMethodInjected = true;
                injectObject = injectionMethod;
              } else {
                inject = false;
              }
              break;
            case ITEST_CONTEXT:
              injectObject = context;
              break;
            case ITEST_RESULT:
              injectObject = testResult;
              break;
            case XML_TEST:
              injectObject = context != null ? context.getCurrentXmlTest() : null;
              break;
            default:
              break;
          }
          if (inject) {
            arguments.add(injectObject);
            break;
          }
        }
      }

      if (!inject && resolved.owns(parameter)) {
        // Native injection had its say first, so this cannot be overriding one.
        arguments.add(resolved.resolve(parameter));
        inject = true;
      }

      if (!inject && !queue.backingList.isEmpty()) {
        arguments.add(queue.poll());
      }
    }
    if (!queue.backingList.isEmpty()) {
      String prefix =
          "Missing one or more parameters that are being injected by the data provider. "
              + "Please add the below arguments to the ";
      final String msg;
      if (injectionMethod instanceof Constructor) {
        msg =
            MethodMatcherException.generateMessage(
                prefix + "constructor.",
                (Constructor<?>) injectionMethod,
                queue.backingList.toArray());
      } else if (injectionMethod instanceof Method || injectionMethod == null) {
        msg =
            MethodMatcherException.generateMessage(
                prefix + "method.", (Method) injectionMethod, queue.backingList.toArray());
      } else {
        throw new TestNGException(
            "Injection holder must be a method or constructor, got "
                + injectionMethod.getClass().getName());
      }

      boolean block = RuntimeBehavior.useStrictParameterMatching();
      if (block) {
        throw new MethodMatcherException(msg);
      } else {
        System.err.println(":::WARNING:::\n" + msg);
      }
    }
    final Object[] injectedArray = new Object[arguments.size()];
    return arguments.toArray(injectedArray);
  }

  /**
   * Injects appropriate arguments.
   *
   * @param parameters array of parameter instances under question.
   * @param filters filters to use.
   * @param args user supplied arguments.
   * @param constructor current test method.
   * @param context current test context.
   * @param testResult on going test results.
   * @return injected arguments.
   */
  public static Object[] inject(
      final Parameter[] parameters,
      final Set<InjectableParameter> filters,
      final Object[] args,
      final Constructor<?> constructor,
      final @Nullable ITestContext context,
      final @Nullable ITestResult testResult) {
    return nativelyInject(
        parameters, filters, ResolvedParameters.none(), args, constructor, context, testResult);
  }

  /**
   * Whether TestNG itself supplies this parameter, by type. This is what makes native injection
   * outrank an {@link org.testng.IParameterResolver}: a parameter answering {@code true} here is
   * never offered to the resolvers. A parameter carrying {@link NoInjection} answers {@code false},
   * which is that annotation's existing meaning -- TestNG stops owning it.
   *
   * @param parameter the parameter under question.
   * @return whether one of {@link InjectableParameter} covers it.
   */
  public static boolean isNativelyInjectable(final Parameter parameter) {
    if (parameter.isAnnotationPresent(NoInjection.class)) {
      return false;
    }
    final Class<?> type = parameter.getType();
    return isOrExtends(Method.class, type)
        || isOrImplementsInterface(ITestContext.class, type)
        || isOrImplementsInterface(ITestResult.class, type)
        || isOrExtends(XmlTest.class, type);
  }

  private static boolean canInject(
      final Parameter parameter, final InjectableParameter injectableParameter) {
    boolean canInject = false;
    if (parameter != null) {
      final boolean inject = !parameter.isAnnotationPresent(NoInjection.class);
      switch (injectableParameter) {
        case CURRENT_TEST_METHOD:
          final boolean isMethod = isOrExtends(Method.class, parameter.getType());
          canInject = inject && isMethod;
          break;
        case ITEST_CONTEXT:
          canInject = inject && isOrImplementsInterface(ITestContext.class, parameter.getType());
          break;
        case ITEST_RESULT:
          canInject = inject && isOrImplementsInterface(ITestResult.class, parameter.getType());
          break;
        case XML_TEST:
          canInject = inject && isOrExtends(XmlTest.class, parameter.getType());
          break;
        default:
          canInject = false;
          break;
      }
    }
    return canInject;
  }

  private static class ListBackedImmutableQueue<T> {
    private final List<T> backingList;

    ListBackedImmutableQueue(final T[] elements) {
      backingList = new ArrayList<>(elements.length);
      Collections.addAll(backingList, elements);
    }

    T poll() {
      if (!backingList.isEmpty()) {
        return backingList.remove(0);
      }
      throw new TestNGException("Queue exhausted");
    }
  }
}
