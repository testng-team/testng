package org.testng.internal.reflect;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestNGException;
import org.testng.annotations.NoInjection;
import org.testng.xml.XmlTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Utility class to handle reflection.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public final class ReflectionRecipes {

  private static final Map<Class, Class> PRIMITIVE_MAPPING = new HashMap<>();
  private static final Map<Class, List<Class>> ASSIGNABLE_MAPPING = new HashMap<>();

  static {
    initPrimitiveMapping();
    initAssignableMapping();
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
    ASSIGNABLE_MAPPING.put(double.class,
        Arrays.<Class>asList(Float.class, Long.class, Integer.class, Short.class,Character.class,Byte.class));
    ASSIGNABLE_MAPPING.put(float.class,
        Arrays.<Class>asList(Long.class, Integer.class, Short.class, Character.class,Byte.class));
    ASSIGNABLE_MAPPING.put(long.class, Arrays.<Class>asList(Integer.class, Short.class, Character.class,Byte.class));
    ASSIGNABLE_MAPPING.put(int.class, Arrays.<Class>asList(Short.class, Character.class,Byte.class));
    ASSIGNABLE_MAPPING.put(short.class, Arrays.<Class>asList(Character.class, Byte.class));
  }

  private ReflectionRecipes() {
    throw new TestNGException("Service is not meant to have instances");
  }

  /**
   * Checks if an instance is an instance of the given class.
   *
   * @param reference  reference class.
   * @param object instance to be tested.
   * @return is an instance-of or not
   */
  public static boolean isInstanceOf(final Class reference, final Object object) {
    if (object == null) {
      return !reference.isPrimitive();
    }
    boolean isInstanceOf;
    final boolean directInstance = reference.isInstance(object);
    if (!directInstance && reference.isPrimitive()) {
      isInstanceOf = PRIMITIVE_MAPPING.get(reference).isInstance(object);
      if (!isInstanceOf) {
        isInstanceOf = ASSIGNABLE_MAPPING.get(reference).contains(object.getClass());
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
   * @param clazz     class instance to be tested.
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
          if (implementsInterface) break;
        }
      }
    }
    return implementsInterface;
  }

  /**
   * Checks a class instance for being a given class or its sub-class.
   *
   * @param reference reference class instance.
   * @param clazz     class instance to be tested.
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
  public static Parameter[] getMethodParameters(final Method method) {
    if (method == null) {
      return new Parameter[]{};
    }
    return getParameters(method.getParameterTypes(), method.getParameterAnnotations());
  }

  /**
   * Extracts constructor parameters.
   *
   * @param constructor any valid constructor.
   * @return extracted constructor parameters.
   */
  public static Parameter[] getConstructorParameters(final Constructor constructor) {
    if (constructor == null) {
      return new Parameter[]{};
    }
    return getParameters(constructor.getParameterTypes(), constructor.getParameterAnnotations());
  }

  private static Parameter[] getParameters(Class<?>[] parametersTypes, final Annotation[][] parametersAnnotations) {
    final Parameter[] parameters = new Parameter[parametersTypes.length];
    for (int i = 0; i < parametersTypes.length; i++) {
      parameters[i] = new Parameter(i, parametersTypes[i], parametersAnnotations[i]);
    }
    return parameters;
  }

  /**
   * @return matches or not
   * @see #matchArrayEnding(Class[], Object[])
   */
  public static boolean matchArrayEnding(final Parameter[] parameters, final Object[] param) {
    return matchArrayEnding(classesFromParameters(parameters), param);
  }

  /**
   * Matches an array of class instances to an array of instances having last class instance an array.
   * <p>
   * Assuming upper case letters denote classes and corresponding lowercase its instances. Classes {A,B,C...},
   * instances {a,b,c1,c2} ==> check for {a,b,{c1,c2}} match or Classes {A,B,C[]}, instances {a,b,c1,c2} ==> check for
   * {a,b,{c1,c2}} match both of the above cases are equivalent.
   *
   * @param classes array of class instances to check against.
   * @param args    instances to be verified.
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
        if (!matching) break;
      }
    } else {
      matching = false;
    }

    if (matching) {
      final Class<?> componentType = classes[classes.length - 1].getComponentType();
      for (; i < args.length; i++) {
        matching = ReflectionRecipes.isInstanceOf(componentType, args[i]);
        if (!matching) break;
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
   * @param args    instances to be verified.
   * @return matches or not
   */
  public static boolean exactMatch(final Class<?>[] classes, final Object[] args) {
    boolean matching = true;
    if (classes.length == args.length) {
      int i = 0;
      for (final Class<?> clazz : classes) {
        matching = ReflectionRecipes.isInstanceOf(clazz, args[i]);
        i++;
        if (!matching) break;
      }
    } else {
      matching = false;
    }
    return matching;
  }

  /**
   * Matches an array of parameters to an array of instances.
   *
   * @return matches or not
   * @see #lenientMatch(Class[], Object[])
   */
  public static boolean lenientMatch(final Parameter[] parameters, final Object[] args) {
    return lenientMatch(classesFromParameters(parameters), args);
  }

  /**
   * Matches an array of class instances to an array of instances. Such that {int, boolean, float} matches {int,
   * boolean}
   *
   * @param classes array of class instances to check against.
   * @param args    instances to be verified.
   * @return matches or not
   */
  public static boolean lenientMatch(final Class<?>[] classes, final Object[] args) {
    boolean matching = true;
    int i = 0;
    for (final Class<?> clazz : classes) {
      matching = ReflectionRecipes.isInstanceOf(clazz, args[i]);
      i++;
      if (!matching) break;
    }
    return matching;
  }

  /**
   * Omits 1. org.testng.ITestContext or its implementations from input array 2. org.testng.ITestResult or its
   * implementations from input array 3. org.testng.xml.XmlTest or its implementations from input array 4. First
   * method depending on filters.
   * <p>
   * An example would be Input: {ITestContext.class, int.class, Boolean.class, TestContext.class} Output: {int.class,
   * Boolean.class}
   *
   * @param parameters array of parameter instances under question.
   * @param filters    filters to use.
   * @return Injects free array of class instances.
   */
  public static Parameter[] filter(final Parameter[] parameters, final Set<InjectableParameter> filters) {
    if (filters != null && !filters.isEmpty()) {
      boolean firstMethodFiltered = false;
      final List<Parameter> filterList = new ArrayList<>(parameters.length);
      for (final Parameter parameter : parameters) {
        boolean omit = false;
        for (final InjectableParameter injectableParameter : filters) {
          omit = canInject(parameter, injectableParameter);
          switch (injectableParameter) {
            case CURRENT_TEST_METHOD:
              if (omit && !firstMethodFiltered) {
                firstMethodFiltered = true;
              } else {
                omit = false;
              }
              break;
            default:
              break;
          }
          if (omit) {
            break;
          }
        }
        if (!omit) {
          filterList.add(parameter);
        }
      }
      final Parameter[] filteredArray = new Parameter[filterList.size()];
      return filterList.toArray(filteredArray);
    } else {
      return parameters;
    }
  }

  /**
   * Injects appropriate arguments.
   *
   * @param parameters      array of parameter instances under question.
   * @param filters         filters to use.
   * @param args            user supplied arguments.
   * @param injectionMethod current test method.
   * @param context         current test context.
   * @param testResult      on going test results.
   * @return injected arguments.
   */
  public static Object[] inject(final Parameter[] parameters, final Set<InjectableParameter> filters,
                                final Object[] args,
                                final Method injectionMethod,
                                final ITestContext context,
                                final ITestResult testResult) {
    return nativelyInject(parameters, filters, args, injectionMethod, context, testResult);
  }

  private static Object[] nativelyInject(final Parameter[] parameters, final Set<InjectableParameter> filters,
                                         final Object[] args,
                                         final Object injectionMethod,
                                         final ITestContext context,
                                         final ITestResult testResult) {
    if (filters == null || filters.isEmpty()) {
      return args;
    }
    final ArrayList<Object> arguments = new ArrayList<>(args.length);
    final ListBackedImmutableQueue<Object> queue = new ListBackedImmutableQueue<>(args);
    boolean firstMethodInjected = false;
    for (final Parameter parameter : parameters) {
      boolean inject = false;
      Object injectObject = null;
      for (final InjectableParameter injectableParameter : filters) {
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
      
      if (!inject && !queue.backingList.isEmpty()) {
        arguments.add(queue.poll());
      }
    }
    if (!queue.backingList.isEmpty()) {
      String prefix = "Missing one or more parameters that are being injected by the data provider. " +
              "Please add the below arguments to the ";
      String msg = null;
      if (injectionMethod instanceof Method) {
        msg = MethodMatcherException.generateMessage(prefix + "method.", (Method) injectionMethod, queue.backingList.toArray());
      } else if (injectionMethod instanceof Constructor) {
        msg = MethodMatcherException.generateMessage(prefix + "constructor.", (Constructor) injectionMethod, queue.backingList.toArray());
      }

      boolean block = Boolean.parseBoolean(System.getProperty("strictParameterMatch"));
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
   * @param parameters      array of parameter instances under question.
   * @param filters         filters to use.
   * @param args            user supplied arguments.
   * @param constructor current test method.
   * @param context         current test context.
   * @param testResult      on going test results.
   * @return injected arguments.
   */
  public static Object[] inject(final Parameter[] parameters, final Set<InjectableParameter> filters,
                                final Object[] args,
                                final Constructor constructor,
                                final ITestContext context,
                                final ITestResult testResult) {
    return nativelyInject(parameters, filters, args, constructor, context, testResult);
  }

  private static boolean canInject(final Parameter parameter, final InjectableParameter injectableParameter) {
    boolean canInject = false;
    if (parameter != null ) {
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
