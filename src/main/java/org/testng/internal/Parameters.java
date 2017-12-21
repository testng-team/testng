package org.testng.internal;

import com.google.inject.Injector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.testng.IDataProviderListener;
import org.testng.IDataProviderMethod;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestNGException;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IParameterizable;
import org.testng.annotations.IParametersAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.ParameterHolder.ParameterOrigin;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAfterClass;
import org.testng.internal.annotations.IAfterGroups;
import org.testng.internal.annotations.IAfterMethod;
import org.testng.internal.annotations.IAfterSuite;
import org.testng.internal.annotations.IAfterTest;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IBeforeClass;
import org.testng.internal.annotations.IBeforeGroups;
import org.testng.internal.annotations.IBeforeMethod;
import org.testng.internal.annotations.IBeforeSuite;
import org.testng.internal.annotations.IBeforeTest;
import org.testng.internal.annotations.IDataProvidable;
import org.testng.internal.collections.ArrayIterator;
import org.testng.internal.reflect.DataProviderMethodMatcher;
import org.testng.internal.reflect.InjectableParameter;
import org.testng.internal.reflect.MethodMatcher;
import org.testng.internal.reflect.MethodMatcherContext;
import org.testng.internal.reflect.Parameter;
import org.testng.internal.reflect.ReflectionRecipes;
import org.testng.util.Strings;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.testng.annotations.*;

/**
 * Methods that bind parameters declared in testng.xml to actual values
 * used to invoke methods.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class Parameters {
  public static final String NULL_VALUE= "null";

  private static final Map<Class<? extends Annotation>, Class<? extends IAnnotation>> ANNOTATION_MAP =
          new ConcurrentHashMap<>();
  static {
    ANNOTATION_MAP.put(BeforeSuite.class, IBeforeSuite.class);
    ANNOTATION_MAP.put(AfterSuite.class, IAfterSuite.class);
    ANNOTATION_MAP.put(BeforeTest.class, IBeforeTest.class);
    ANNOTATION_MAP.put(AfterTest.class, IAfterTest.class);
    ANNOTATION_MAP.put(BeforeClass.class, IBeforeClass.class);
    ANNOTATION_MAP.put(AfterClass.class, IAfterClass.class);
    ANNOTATION_MAP.put(BeforeGroups.class, IBeforeGroups.class);
    ANNOTATION_MAP.put(AfterGroups.class, IAfterGroups.class);
    ANNOTATION_MAP.put(BeforeMethod.class, IBeforeMethod.class);
    ANNOTATION_MAP.put(AfterMethod.class, IAfterMethod.class);
  }

  private static Class<?>[] annotationList = new Class<?>[] {
      BeforeSuite.class,
      AfterSuite.class,
      BeforeTest.class,
      AfterTest.class,
      BeforeClass.class,
      AfterClass.class,
      BeforeGroups.class,
      AfterGroups.class,
      BeforeMethod.class,
      AfterMethod.class
  };

  private static Map<String, List<Class<?>>> mapping = Maps.newHashMap();
/*
          +--------------+--------------+---------+--------+----------+-------------+
          |  Annotation  | ITestContext | XmlTest | Method | Object[] | ITestResult |
          +--------------+--------------+---------+--------+----------+-------------+
          | BeforeSuite  | Yes          | Yes     | No     | No       | No          |
          +--------------+--------------+---------+--------+----------+-------------+
          | BeforeTest   | Yes          | Yes     | No     | No       | No          |
          +--------------+--------------+---------+--------+----------+-------------+
          | BeforeGroups | Yes          | Yes     | No     | No       | No          |
          +--------------+--------------+---------+--------+----------+-------------+
          | BeforeClass  | Yes          | Yes     | No     | No       | No          |
          +--------------+--------------+---------+--------+----------+-------------+
          | BeforeMethod | Yes          | Yes     | Yes    | Yes      | Yes         |
          +--------------+--------------+---------+--------+----------+-------------+
          | AfterSuite   | Yes          | Yes     | No     | No       | No          |
          +--------------+--------------+---------+--------+----------+-------------+
          | AfterTest    | Yes          | Yes     | No     | No       | No          |
          +--------------+--------------+---------+--------+----------+-------------+
          | AfterGroups  | Yes          | Yes     | No     | No       | No          |
          +--------------+--------------+---------+--------+----------+-------------+
          | AfterClass   | Yes          | Yes     | No     | No       | No          |
          +--------------+--------------+---------+--------+----------+-------------+
          | AfterMethod  | Yes          | Yes     | Yes    | Yes      | Yes         |
          +--------------+--------------+---------+--------+----------+-------------+
          | Test         | Yes          | Yes     | Yes    | No       | No          |
          +--------------+--------------+---------+--------+----------+-------------+

 */
  static {
    List<Class<?>> ctxTest = Arrays.asList(ITestContext.class, XmlTest.class);
    List<Class<?>> beforeAfterMethod = Arrays.asList(ITestContext.class, XmlTest.class, Method.class,
            Object[].class, ITestResult.class);
    mapping.put(BeforeSuite.class.getSimpleName(), ctxTest);
    mapping.put(AfterSuite.class.getSimpleName(), ctxTest);

    mapping.put(BeforeTest.class.getSimpleName(), ctxTest);
    mapping.put(AfterTest.class.getSimpleName(), ctxTest);

    mapping.put(BeforeGroups.class.getSimpleName(), ctxTest);
    mapping.put(AfterGroups.class.getSimpleName(), ctxTest);

    mapping.put(BeforeClass.class.getSimpleName(), ctxTest);
    mapping.put(AfterClass.class.getSimpleName(), ctxTest);

    mapping.put(BeforeMethod.class.getSimpleName(), beforeAfterMethod);
    mapping.put(AfterMethod.class.getSimpleName(), beforeAfterMethod);
    mapping.put(Test.class.getSimpleName(), Arrays.<Class<?>>asList(ITestContext.class, XmlTest.class, Method.class));

  }

  /**
   * Creates the parameters needed for constructing a test class instance.
   * @param finder TODO
   */
  public static Object[] createInstantiationParameters(Constructor ctor,
      String methodAnnotation,
      IAnnotationFinder finder,
      String[] parameterNames,
      Map<String, String> params, XmlSuite xmlSuite)
  {

    return createParametersForConstructor(ctor, ctor.getParameterTypes(),
        finder.findOptionalValues(ctor), methodAnnotation, parameterNames,
            new MethodParameters(params, Collections.<String, String>emptyMap()),
            xmlSuite);
  }

  /**
   * Creates the parameters needed for the specified <tt>@Configuration</tt> <code>Method</code>.
   *
   * @param m the configuraton method
   * @param currentTestMethod the current @Test method or <code>null</code> if no @Test is available (this is not
   *    only in case the configuration method is a @Before/@AfterMethod
   * @param finder the annotation finder
   */
  public static Object[] createConfigurationParameters(Method m,
      Map<String, String> params,
      Object[] parameterValues,
      @Nullable ITestNGMethod currentTestMethod,
      IAnnotationFinder finder,
      XmlSuite xmlSuite,
      ITestContext ctx,
      ITestResult testResult)
  {
    Method currentTestMeth= currentTestMethod != null ?
        currentTestMethod.getConstructorOrMethod().getMethod() : null;

    Map<String, String> methodParams = currentTestMethod != null
        ? currentTestMethod.findMethodParameters(ctx.getCurrentXmlTest())
        : Collections.<String, String>emptyMap();

    Class<? extends Annotation> annotation = retrieveConfigAnnotation(m);
    String name = annotation == null ? "" : annotation.getSimpleName();
    Class<? extends IAnnotation> annotationClass = null;
    if (annotation != null) {
      annotationClass = ANNOTATION_MAP.get(annotation);
    }

    return createParameters(m,
        new MethodParameters(params,
            methodParams,
            parameterValues,
            currentTestMeth, ctx, testResult),
        finder, xmlSuite, annotationClass, name);
  }

  private static Class<? extends Annotation> retrieveConfigAnnotation(Method m) {
    for (Class annotation : annotationList) {
      if (m.getAnnotation(annotation) != null) {
        return annotation;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////

  /**
   * @deprecated - This method stands deprecated as of TestNG v6.11. There are no alternatives.
   */
  @Deprecated
  public static Object getInjectedParameter(Class<?> c, Method method, ITestContext context,
      ITestResult testResult) {
    Object result = null;
    if (Method.class.equals(c)) {
      result = method;
    }
    else if (ITestContext.class.equals(c)) {
      result = context;
    }
    else if (XmlTest.class.equals(c)) {
      result = context.getCurrentXmlTest();
    }
    else if (ITestResult.class.equals(c)) {
      result = testResult;
    }
    return result;
  }

  private static Object[] createParametersForConstructor(Constructor constructor,
                                                         Class<?>[] parameterTypes,
                                                         String[] optionalValues,
                                                         String methodAnnotation,
                                                         String[] parameterNames, MethodParameters params, XmlSuite xmlSuite) {
    if (parameterTypes.length == 0) {
      return new Object[0];
    }

    checkParameterTypes(constructor.getName(), parameterTypes, methodAnnotation, parameterNames);
    List<Object> vResult = Lists.newArrayList();

    if (canInject(methodAnnotation)) {
      Parameter[] paramsArray = ReflectionRecipes.getConstructorParameters(constructor);
      Object[] inject = ReflectionRecipes.inject(paramsArray, InjectableParameter.Assistant.ALL_INJECTS,
              new Object[0], constructor, params.context, params.testResult);
      if (inject != null) {
        vResult.addAll(Arrays.asList(inject));
      }
    }
    List<Object> consParams = createParams(constructor.getName(), "constructor", methodAnnotation,
            parameterTypes, optionalValues, parameterNames, params, xmlSuite);
    vResult.addAll(consParams);

    return vResult.toArray(new Object[vResult.size()]);
  }

  private static List<Object> createParams(String name,
                                       String prefix,
                                       String methodAnnotation,
                                       Class<?>[] parameterTypes,
                                       String[] optionalValues,
                                       String[] parameterNames,
                                       MethodParameters params,
                                       XmlSuite xmlSuite) {
    List<Object> vResult = Lists.newArrayList();
    if (optionalValues.length != parameterNames.length) {
      FilterOutInJectedTypesResult filterOutResult = filterOutInJectedTypesFromOptionalValues(parameterTypes, optionalValues);
      optionalValues = filterOutResult.getOptionalValues();
      parameterTypes = filterOutResult.getParameterTypes();
    }
    for (int i = 0; i < parameterNames.length; i++) {
      String p = parameterNames[i];
      String value = params.xmlParameters.get(p);
      if (null == value) {
        // try SysEnv entries
        value = System.getProperty(p);
      }
      if (null == value) {
        if (optionalValues != null) {
            value = optionalValues[i];
        }
        if (null == value) {
            throw new TestNGException("Parameter '" + p + "' is required by "
                    + methodAnnotation
                    + " on " + prefix + " "
                    + name
                    + " but has not been marked @Optional or defined\n"
                    + (xmlSuite.getFileName() != null ? "in "
                            + xmlSuite.getFileName() : ""));
        }
    }
      vResult.add(convertType(parameterTypes[i], value, p));
    }
    return vResult;
  }
  
  /**
   * Remove injected types from parameterTypes and optionalValues
   * @param parameterTypes
   * @param optionalValues
   * @return FilterOutInJectedTypesResult
   */
  static FilterOutInJectedTypesResult filterOutInJectedTypesFromOptionalValues(Class<?>[] parameterTypes, String[] optionalValues) {
    List<Class<?>> typeList = Lists.newArrayList(parameterTypes);
    List<String> optionalValueList = Lists.newArrayList(optionalValues);
    Iterator<Class<?>> typeIterator = typeList.iterator();
    Iterator<String> optionalIterator = optionalValueList.iterator();
    while (typeIterator.hasNext()) {
        Class<?> parameterType = typeIterator.next();
        optionalIterator.next();
        if (INJECTED_TYPES.contains(parameterType)) {
            optionalIterator.remove();
            typeIterator.remove();
        }
    }
    return new FilterOutInJectedTypesResult(typeList.toArray(new Class<?>[typeList.size()]),
            optionalValueList.toArray(new String[optionalValueList.size()]));
  }

  /**
   * Store the result of parameterTypes and optionalValues after filter out injected types
   */
  final static class FilterOutInJectedTypesResult {
    private Class<?>[] parameterTypes;
    private String[] optionalValues;

    private FilterOutInJectedTypesResult(Class<?>[] parameterTypes, String[] optionalValues) {
        this.parameterTypes = parameterTypes;
        this.optionalValues = optionalValues;
    }

    Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    String[] getOptionalValues() {
        return optionalValues;
    }
  }

  /**
   * @return An array of parameters suitable to invoke this method, possibly
   * picked from the property file
   */
  private static Object[] createParametersForMethod(Method method,
      Class<?>[] parameterTypes,
      String[] optionalValues,
      String methodAnnotation,
      String[] parameterNames, MethodParameters params, XmlSuite xmlSuite)
  {
    if (parameterTypes.length == 0) {
      return new Object[0];
    }

    checkParameterTypes(method.getName(), parameterTypes, methodAnnotation, parameterNames);
    List<Object> vResult = Lists.newArrayList();

    List<Object> consParams = createParams(method.getName(), "method", methodAnnotation, parameterTypes,
            optionalValues, parameterNames, params, xmlSuite);
    if (canInject(methodAnnotation)) {
      Parameter[] paramsArray = ReflectionRecipes.getMethodParameters(method);
      Object[] inject = ReflectionRecipes.inject(paramsArray, InjectableParameter.Assistant.ALL_INJECTS,
              consParams.toArray(new Object[consParams.size()]), params.currentTestMethod, params.context, params.testResult);
      if (inject != null) {
        vResult.addAll(Arrays.asList(inject));
      }
    } else {
      vResult.addAll(consParams);
    }
    return vResult.toArray(new Object[vResult.size()]);
  }

  private static boolean canInject(String annotation) {
    return !("@" + Test.class.getSimpleName()).equalsIgnoreCase(annotation);
  }

  private static final List<Class<?>> INJECTED_TYPES = Arrays.asList(
      ITestContext.class, ITestResult.class, XmlTest.class, Method.class, Object[].class
  );
  private static void checkParameterTypes(String methodName,
      Class<?>[] parameterTypes, String methodAnnotation, String[] parameterNames)
  {
    int totalLength = parameterTypes.length;
    for (Class parameterType : parameterTypes) {
      if (INJECTED_TYPES.contains(parameterType)) {
        totalLength--;
      }
    }
    if (parameterNames.length == 0) {
      //parameterNames is usually populated via the @Parameters annotation, so we would need to
      //apply our logic only when @Parameters annotation is not involved.
      boolean invalid = (totalLength != 0) || (!validParameters(methodAnnotation, parameterTypes));
      if (invalid) {
        String annotation = methodAnnotation;
        if (!methodAnnotation.startsWith("@")) {
          annotation = "@" + methodAnnotation;
        }
        String errPrefix;
        if (mapping.containsKey(methodAnnotation)) {
          errPrefix = "Can inject only one of " + prettyFormat(mapping.get(methodAnnotation)) +
                  " into a " + annotation + " annotated " + methodName;
        } else {
          errPrefix = "Cannot inject " + annotation + " annotated Method [" + methodName + "] with "
                  + Arrays.toString(parameterTypes);
        }
        throw new TestNGException(errPrefix
                + ".\nFor more information on native dependency injection please refer to " +
                "http://testng.org/doc/documentation-main.html#native-dependency-injection"
        );
      }
    }

    if (parameterNames.length != totalLength) {
      throw new TestNGException( "Method " + methodName + " requires "
          + parameterTypes.length + " parameters but "
          + parameterNames.length
          + " were supplied in the "
          + methodAnnotation
          + " annotation.");
    }
  }

  private static boolean validParameters(String methodAnnotation, Class[] parameterTypes) {
    List<Class<?>> localMapping = mapping.get(methodAnnotation.replace("@", ""));
    if (localMapping == null) {
      return false;
    }
    for (Class<?> parameterType : parameterTypes) {
      if (!localMapping.contains(parameterType)) {
        return false;
      }
    }
    return true;
  }

  private static String prettyFormat(List<Class<?>> classes) {
    StringBuilder builder = new StringBuilder("<");
    if (classes.size() == 1) {
      builder.append(classes.get(0));
    } else {
      int length = classes.size();
      for (int i=0; i < length - 1; i++) {
        builder.append(classes.get(i).getSimpleName()).append(", ");
      }
      builder.append(classes.get(length-1).getSimpleName());
    }
    builder.append(">");
    return builder.toString();
  }

  public static <T> T convertType(Class<T> type, String value, String paramName) {
    try {
      if (value == null || NULL_VALUE.equals(value.toLowerCase())) {
        if(type.isPrimitive()) {
          Utils.log("Parameters", 2, "Attempt to pass null value to primitive type parameter '" + paramName + "'");
        }

        return null; // null value must be used
      }

      if(type == String.class) {
        return (T) value;
      }
      if (type == int.class || type == Integer.class) {
        return (T) Integer.valueOf(value);
      }
      if (type == boolean.class || type == Boolean.class) {
        return (T) Boolean.valueOf(value);
      }
      if (type == byte.class || type == Byte.class) {
        return (T) Byte.valueOf(value);
      }
      if (type == char.class || type == Character.class) {
        return (T) Character.valueOf(value.charAt(0));
      }
      if (type == double.class || type == Double.class) {
        return (T) Double.valueOf(value);
      }
      if (type == float.class || type == Float.class) {
        return (T) Float.valueOf(value);
      }
      if (type == long.class || type == Long.class) {
        return (T) Long.valueOf(value);
      }
      if (type == short.class || type == Short.class) {
        return (T) Short.valueOf(value);
      }
      if (type.isEnum()) {
        return (T) Enum.valueOf((Class<Enum>) type, value);
      }
    } catch (Exception e) {
      throw new TestNGException("Conversion issue on parameter: " + paramName, e);
    }
    throw new TestNGException("Unsupported type parameter : " + type);
  }

  private static IDataProviderMethod findDataProvider(Object instance, ITestClass clazz,
                                                     ConstructorOrMethod m,
                                                     IAnnotationFinder finder, ITestContext context) {
    IDataProviderMethod result = null;

    IDataProvidable dp = findDataProviderInfo(clazz, m, finder);
    if (dp != null) {
      String dataProviderName = dp.getDataProvider();
      Class dataProviderClass = dp.getDataProviderClass();

      if (! Utils.isStringEmpty(dataProviderName)) {
        result = findDataProvider(instance, clazz, finder, dataProviderName, dataProviderClass, context);

        if(null == result) {
          throw new TestNGException("Method " + m + " requires a @DataProvider named : "
              + dataProviderName + (dataProviderClass != null ? " in class " + dataProviderClass.getName() : "")
              );
        }
      }
    }

    return result;
  }

  /**
   * Find the data provider info (data provider name and class) on either @Test(dataProvider),
   * @Factory(dataProvider) on a method or @Factory(dataProvider) on a constructor.
   */
  private static IDataProvidable findDataProviderInfo(ITestClass clazz, ConstructorOrMethod m,
      IAnnotationFinder finder) {
    IDataProvidable result;

    if (m.getMethod() != null) {
      //
      // @Test(dataProvider) on a method
      //
      result = AnnotationHelper.findTest(finder, m.getMethod());
      if (result == null) {
        //
        // @Factory(dataProvider) on a method
        //
        result = AnnotationHelper.findFactory(finder, m.getMethod());
      }
      if (result == null) {
        //
        // @Test(dataProvider) on a class
        result = AnnotationHelper.findTest(finder, clazz.getRealClass());
      }
    } else {
      //
      // @Factory(dataProvider) on a constructor
      //
      result = AnnotationHelper.findFactory(finder, m.getConstructor());
    }

    return result;
  }

  /**
   * Find a method that has a @DataProvider(name=name)
   */
  private static IDataProviderMethod findDataProvider(Object instance, ITestClass clazz,
                                                      IAnnotationFinder finder,
                                                      String name, Class<?> dataProviderClass,
                                                      ITestContext context)
  {
    IDataProviderMethod result = null;

    Class<?> cls = clazz.getRealClass();
    boolean shouldBeStatic = false;
    if (dataProviderClass != null) {
      cls = dataProviderClass;
      shouldBeStatic = true;
    }

    for (Method m : ClassHelper.getAvailableMethods(cls)) {
      IDataProviderAnnotation dp = finder.findAnnotation(m, IDataProviderAnnotation.class);
      if (null != dp && name.equals(getDataProviderName(dp, m))) {
        Object instanceToUse;
        if (shouldBeStatic && (m.getModifiers() & Modifier.STATIC) == 0) {
          Injector injector = context.getInjector(clazz);
          if (injector != null) {
            instanceToUse = injector.getInstance(dataProviderClass);
          } else {
            instanceToUse = ClassHelper.newInstance(dataProviderClass);
          }
        } else {
          instanceToUse = instance;
        }
        // Not a static method but no instance exists, then create new one if possible
        if ((m.getModifiers() & Modifier.STATIC) == 0 && instanceToUse == null) {
          instanceToUse = ClassHelper.newInstanceOrNull(cls);
        }

        if (result != null) {
          throw new TestNGException("Found two providers called '" + name + "' on " + cls);
        }
        result = new DataProviderMethod(instanceToUse, m, dp);
      }
    }

    return result;
  }

  private static String getDataProviderName(IDataProviderAnnotation dp, Method m) {
	  return Strings.isNullOrEmpty(dp.getName()) ? m.getName() : dp.getName();
  }

  @SuppressWarnings({"deprecation"})
  private static Object[] createParameters(Method m, MethodParameters params,
      IAnnotationFinder finder, XmlSuite xmlSuite, Class annotationClass, String atName)
  {
    List<Object> result = Lists.newArrayList();

    Object[] extraParameters;
    //
    // Try to find an @Parameters annotation
    //
    IParametersAnnotation annotation = finder.findAnnotation(m, IParametersAnnotation.class);
    Class<?>[] types = m.getParameterTypes();
    if(null != annotation) {
      String[] parameterNames = annotation.getValue();
      extraParameters = createParametersForMethod(m, types,
          finder.findOptionalValues(m), atName, parameterNames, params, xmlSuite);
    }

    //
    // Else, use the deprecated syntax
    //
    else {
      IParameterizable a = (IParameterizable) finder.findAnnotation(m, annotationClass);
      if(null != a && a.getParameters() != null && a.getParameters().length > 0) {
        String[] parameterNames = a.getParameters();
        extraParameters = createParametersForMethod(m, types,
            finder.findOptionalValues(m), atName, parameterNames, params, xmlSuite);
      }
      else {
        extraParameters = createParametersForMethod(m, types, finder.findOptionalValues(m), atName, new String[0], params, xmlSuite);
      }
    }

    //
    // Add the extra parameters we found
    //
    Collections.addAll(result, extraParameters);

    // If the method declared an Object[] parameter and we have parameter values, inject them
    for (int i = 0; i < types.length; i++) {
        if (Object[].class.equals(types[i])) {
            result.add(i, params.parameterValues);
        }
    }


    return result.toArray(new Object[result.size()]);
  }

  /**
   * If the method has parameters, fill them in. Either by using a @DataProvider
   * if any was provided, or by looking up <parameters> in testng.xml
   * @return An Iterator over the values for each parameter of this
   * method.
   */
  public static ParameterHolder handleParameters(final ITestNGMethod testMethod,
      Map<String, String> allParameterNames,
      Object instance,
      MethodParameters methodParams,
      XmlSuite xmlSuite,
      IAnnotationFinder annotationFinder,
      Object fedInstance) {
    return handleParameters(testMethod, allParameterNames, instance, methodParams, xmlSuite, annotationFinder, fedInstance,
            Collections.<IDataProviderListener>emptyList());
  }

  /**
   * If the method has parameters, fill them in. Either by using a @DataProvider
   * if any was provided, or by looking up <parameters> in testng.xml
   * @return An Iterator over the values for each parameter of this
   * method.
   */
  public static ParameterHolder handleParameters(final ITestNGMethod testMethod,
                                                 final Map<String, String> allParameterNames,
                                                 final Object instance,
                                                 final MethodParameters methodParams,
                                                 final XmlSuite xmlSuite,
                                                 final IAnnotationFinder annotationFinder,
                                                 final Object fedInstance,
                                                 final Collection<IDataProviderListener> dataProviderListeners) {
    /*
     * Do we have a @DataProvider? If yes, then we have several
     * sets of parameters for this method
     */
    final IDataProviderMethod dataProviderMethod =
            findDataProvider(instance, testMethod.getTestClass(),
                    testMethod.getConstructorOrMethod(), annotationFinder, methodParams.context);

    if (null != dataProviderMethod) {
      int parameterCount = testMethod.getConstructorOrMethod().getParameterTypes().length;

      for (int i = 0; i < parameterCount; i++) {
        String n = "param" + i;
        allParameterNames.put(n, n);
      }

      for (IDataProviderListener dataProviderListener : dataProviderListeners) {
        dataProviderListener.beforeDataProviderExecution(dataProviderMethod, testMethod, methodParams.context);
      }

      final Iterator<Object[]> parameters = MethodInvocationHelper.invokeDataProvider(
              dataProviderMethod.getInstance(), /* a test instance or null if the dataprovider is static*/
              dataProviderMethod.getMethod(),
              testMethod,
              methodParams.context,
              fedInstance,
              annotationFinder);

      for (IDataProviderListener dataProviderListener : dataProviderListeners) {
        dataProviderListener.afterDataProviderExecution(dataProviderMethod, testMethod, methodParams.context);
      }

      // If the data provider is restricting the indices to return, filter them out
      final List<Integer> allIndices = new ArrayList<>();
      allIndices.addAll(testMethod.getInvocationNumbers());
      allIndices.addAll(dataProviderMethod.getIndices());

      final Iterator<Object[]> filteredParameters = new Iterator<Object[]>() {
        int index = 0;
        boolean hasWarn = false;

        @Override
        public boolean hasNext() {
          if (index == 0 && !parameters.hasNext() && !hasWarn) {
            hasWarn = true;
            Utils.log("", 2,  "Warning: the data provider '" + dataProviderMethod.getName() + "' returned an empty array or iterator, so this test is not doing anything");
          }
          return parameters.hasNext();
        }

        @Override
        public Object[] next() {
          testMethod.setParameterInvocationCount(index);
          Object[] next = parameters.next();
          if (!allIndices.isEmpty() && !allIndices.contains(index)) {
            next = null;
          }
          index++;
          return next;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException("remove");
        }
      };

      testMethod.setMoreInvocationChecker(new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          return filteredParameters.hasNext();
        }
      });

      return new ParameterHolder(filteredParameters, ParameterOrigin.ORIGIN_DATA_PROVIDER, dataProviderMethod);
    }
    else {
      //
      // Normal case: we have only one set of parameters coming from testng.xml
      //
      allParameterNames.putAll(methodParams.xmlParameters);
      // Create an Object[][] containing just one row of parameters
      Object[][] allParameterValuesArray = new Object[1][];
      allParameterValuesArray[0] = createParameters(testMethod.getConstructorOrMethod().getMethod(),
              methodParams, annotationFinder, xmlSuite, ITestAnnotation.class, "@Test");

      // Mark that this method needs to have at least a certain
      // number of invocations (needed later to call AfterGroups
      // at the right time).
      testMethod.setParameterInvocationCount(allParameterValuesArray.length);
      // Turn it into an Iterable
      Iterator<Object[]> parameters = new ArrayIterator(allParameterValuesArray);

      return new ParameterHolder(parameters, ParameterOrigin.ORIGIN_XML, null);
    }
  }

  /**
   * Gets an array of parameter values returned by data provider or the ones that
   * are injected based on parameter type. The method also checks for {@code NoInjection}
   * annotation
   *
   * @param parameterValues parameter values from a data provider
   * @param method method to be invoked
   * @param context test context
   */
  public static Object[] injectParameters(Object[] parameterValues, Method method, ITestContext context)
          throws TestNGException {
    MethodMatcherContext matcherContext = new MethodMatcherContext(method, parameterValues, context, null);
    final MethodMatcher matcher = new DataProviderMethodMatcher(matcherContext);
    return matcher.getConformingArguments();
  }

  public static Object[] getParametersFromIndex(Iterator<Object[]> parametersValues, int index) {
    while (parametersValues.hasNext()) {
      Object[] parameters = parametersValues.next();

      if (index == 0) {
        return parameters;
      }
      index--;
    }
    return null;
  }


  /** A parameter passing helper class. */
  public static class MethodParameters {
    private final Map<String, String> xmlParameters;
    private final Method currentTestMethod;
    private final ITestContext context;
    private Object[] parameterValues;
    public ITestResult testResult;

    public MethodParameters(Map<String, String> params, Map<String, String> methodParams) {
      this(params, methodParams, null, null, null, null);
    }

    public static MethodParameters newInstance(Map<String, String> params, ITestNGMethod testNGMethod,
                                               ITestContext context) {
      Map<String, String> methodParams = testNGMethod.findMethodParameters(context.getCurrentXmlTest());
      Object[] pv = null;
      ITestResult tr = null;
      Method method = testNGMethod.getConstructorOrMethod().getMethod();
      return new MethodParameters(params, methodParams, pv, method, context, tr);
    }


    /**
     * @param params parameters found in the suite and test tags
     * @param methodParams parameters found in the include tag
     * @param pv
     * @param m
     * @param ctx
     * @param tr
     */
    public MethodParameters(Map<String, String> params,
        Map<String, String> methodParams,
        Object[] pv, Method m, ITestContext ctx,
        ITestResult tr) {
      Map<String, String> allParams = Maps.newHashMap();
      allParams.putAll(params);
      allParams.putAll(methodParams);
      xmlParameters = allParams;
      currentTestMethod = m;
      context = ctx;
      parameterValues = pv;
      testResult = tr;
    }
  }
}
