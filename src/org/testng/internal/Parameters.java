package org.testng.internal;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestNGException;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IParameterizable;
import org.testng.annotations.IParametersAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.Sets;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Methods that bind parameters declared in testng.xml to actual values
 * used to invoke methods.
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 *  
 * @@ANNOTATIONS@@
 */
public class Parameters {
  public static final String NULL_VALUE= "null";
  
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
    return createParameters(ctor.toString(), ctor.getParameterTypes(),
        finder.findOptionalValues(ctor), methodAnnotation, finder, parameterNames, new MethodParameters(params), xmlSuite);
  }

  /**
   * Creates the parameters needed for the specified <tt>@Configuration</tt> <code>Method</code>.
   * 
   * @param m the configuraton method
   * @param params 
   * @param currentTestMethod the current @Test method or <code>null</code> if no @Test is available (this is not
   *    only in case the configuration method is a @Before/@AfterMethod
   * @param finder the annotation finder
   * @param xmlSuite
   * @return
   */
  public static Object[] createConfigurationParameters(Method m, 
      Map<String, String> params, 
      Object[] parameterValues,
      ITestNGMethod currentTestMethod,
      IAnnotationFinder finder, 
      XmlSuite xmlSuite,
      ITestContext ctx,
      ITestResult testResult) 
  {
    Method currentTestMeth= currentTestMethod != null ? 
        currentTestMethod.getMethod() : null;
    return createParameters(m,
        new MethodParameters(params, parameterValues, currentTestMeth, ctx, testResult), 
        finder, xmlSuite, IConfigurationAnnotation.class, "@Configuration");
  }

  ////////////////////////////////////////////////////////

  /**
   * @param optionalValues TODO
   * @param finder TODO
   * @param parameterAnnotations TODO
   * @param m
   * @param instance
   * @return An array of parameters suitable to invoke this method, possibly
   * picked from the property file
   */
  private static Object[] createParameters(String methodName,
                                           Class[] parameterTypes,
                                           String[] optionalValues,
                                           String methodAnnotation,
                                           IAnnotationFinder finder,
                                           String[] parameterNames, MethodParameters params, XmlSuite xmlSuite)
  {
    Object[] result = new Object[0];
    if(parameterTypes.length > 0) {
      List<Object> vResult = Lists.newArrayList();
  
      checkParameterTypes(methodName, parameterTypes, methodAnnotation, parameterNames);
  
      for(int i = 0, j = 0; i < parameterTypes.length; i++) {
        if (Method.class.equals(parameterTypes[i])) {
          vResult.add(params.currentTestMethod);
        }
        else if (ITestContext.class.equals(parameterTypes[i])) {
          vResult.add(params.context);
        }
        else if (XmlTest.class.equals(parameterTypes[i])) {
          vResult.add(params.context.getCurrentXmlTest());
        }
        else if (ITestResult.class.equals(parameterTypes[i])) {
          vResult.add(params.testResult);
        }
        else {
          if (j < parameterNames.length) {
            String p = parameterNames[j];
            String value = params.xmlParameters.get(p);
            if(null == value) {
              // try SysEnv entries
              value= System.getProperty(p);
            }
            if (null == value) {
              if (optionalValues != null) {
                value = optionalValues[i];
              }
              if (null == value) {
              throw new TestNGException("Parameter '" + p + "' is required by " 
                  + methodAnnotation
                  + " on method " 
                  + methodName
                  + "\nbut has not been marked @Optional or defined "
                  + (xmlSuite.getFileName() != null ? "in "
                  + xmlSuite.getFileName() : ""));
              }
            }
              
            vResult.add(convertType(parameterTypes[i], value, p));
            j++;
          }
        }
      }
            
      result = (Object[]) vResult.toArray(new Object[vResult.size()]);
    }
  
    return result;
  }

  // FIXME
  private static void checkParameterTypes(String methodName, 
      Class[] parameterTypes, String methodAnnotation, String[] parameterNames) 
  {
    if(parameterNames.length == parameterTypes.length) return;
    
    for(int i= parameterTypes.length - 1; i >= parameterNames.length; i--) {
      Class type = parameterTypes[i];
      if(!ITestContext.class.equals(type)
          && !ITestResult.class.equals(type)
          && !XmlTest.class.equals(type)
          && !Method.class.equals(type)
          && !Object[].class.equals(type)) {
        throw new TestNGException( "Method " + methodName + " requires " 
            + parameterTypes.length + " parameters but " 
            + parameterNames.length
            + " were supplied in the "
            + methodAnnotation
            + " annotation.");        
      }
    }
  }

  //TODO: Cosmin - making this public is not the best solution
  public static Object convertType(Class type, String value, String paramName) {
    Object result = null;
    
    if(NULL_VALUE.equals(value.toLowerCase())) {
      if(type.isPrimitive()) {
        Utils.log("Parameters", 2, "Attempt to pass null value to primitive type parameter '" + paramName + "'");
      }
      
      return null; // null value must be used
    }
    
    if(type == String.class) {
      result = value;
    } 
    else if(type == int.class || type == Integer.class) {
      result = new Integer(Integer.parseInt(value));
    }
    else if(type == boolean.class || type == Boolean.class) {
      result = Boolean.valueOf(value);          
    }
    else if(type == byte.class || type == Byte.class) {
      result = new Byte(Byte.parseByte(value));                    
    }
    else if(type == char.class || type == Character.class) {
      result = new Character(value.charAt(0));                              
    }
    else if(type == double.class || type == Double.class) {
      result = new Double(Double.parseDouble(value));
    }
    else if(type == float.class || type == Float.class) {
      result = new Float(Float.parseFloat(value));          
    }
    else if(type == long.class || type == Long.class) {
      result = new Long(Long.parseLong(value));
    }
    else if(type == short.class || type == Short.class) {
      result = new Short(Short.parseShort(value));
    }
    else {
      assert false : "Unsupported type parameter : " + type;
    }
    
    return result;
  }
  
  private static DataProviderHolder findDataProvider(Class clazz, Method m,
      IAnnotationFinder finder) {
    DataProviderHolder result = null;
    String dataProviderName = null;
    Class dataProviderClass = null;
    
    ITestAnnotation annotation = AnnotationHelper.findTest(finder, m);
    if (annotation == null) {
      annotation = AnnotationHelper.findTest(finder, clazz);
    }
    if (annotation != null) {
      dataProviderName = annotation.getDataProvider();
      dataProviderClass = annotation.getDataProviderClass();
    }
    
    if (dataProviderName == null) {
      IFactoryAnnotation factory = AnnotationHelper.findFactory(finder, m);
      if (factory != null) {
        dataProviderName = factory.getDataProvider();
        dataProviderClass = null;
      }
    }
    
    if (null != dataProviderName && ! "".equals(dataProviderName)) {
      result = findDataProvider(clazz, finder, dataProviderName, dataProviderClass);
      
      if(null == result) {
        throw new TestNGException("Method " + m + " requires a @DataProvider named : " 
            + dataProviderName + (dataProviderClass != null ? " in class " + dataProviderClass.getName() : "") 
            );
      }
    }

    return result;
  }
  
  /**
   * Find a method that has a @DataProvider(name=name)
   */
  private static DataProviderHolder findDataProvider(Class cls, IAnnotationFinder finder,
      String name, Class dataProviderClass) 
  {
    boolean shouldBeStatic = false;
    if (dataProviderClass != null) {
      cls = dataProviderClass;
      shouldBeStatic = true;
    }

    for (Method m : ClassHelper.getAvailableMethods(cls)) {
      IDataProviderAnnotation dp = (IDataProviderAnnotation)
          finder.findAnnotation(m, IDataProviderAnnotation.class);
      if (null != dp && (name.equals(dp.getName()) || name.equals(m.getName()))) {
        if (shouldBeStatic && (m.getModifiers() & Modifier.STATIC) == 0) {
          throw new TestNGException("DataProvider should be static: " + m); 
        }
        
        return new DataProviderHolder(dp, m);
      }
    }
    
    return null;
  }

  @SuppressWarnings({"deprecation"})
  private static Object[] createParameters(Method m, MethodParameters params,
      IAnnotationFinder finder, XmlSuite xmlSuite, Class annotationClass, String atName) 
  {
    List<Object> result = Lists.newArrayList();
    
    Object[] extraParameters = new Object[0];
    //
    // Try to find an @Parameters annotation
    //
    IParametersAnnotation annotation = (IParametersAnnotation) finder.findAnnotation(m, IParametersAnnotation.class);
    Class<?>[] types = m.getParameterTypes();
    if(null != annotation) {
      String[] parameterNames = annotation.getValue();
      extraParameters = createParameters(m.getName(), types, 
          finder.findOptionalValues(m), atName, finder, parameterNames, params, xmlSuite);
    }  
    
    //
    // Else, use the deprecated syntax
    //
    else {    
      IParameterizable a = (IParameterizable) finder.findAnnotation(m, annotationClass);
      if(null != a && a.getParameters().length > 0) {
        String[] parameterNames = a.getParameters();
        extraParameters = createParameters(m.getName(), types, 
            finder.findOptionalValues(m), atName, finder, parameterNames, params, xmlSuite);
      }
      else {
        extraParameters = createParameters(m.getName(), types, 
            finder.findOptionalValues(m), atName, finder, new String[0], params, xmlSuite);
      }
    }
    
    // If the method declared an Object[] parameter and we have parameter values, inject them
    for (int i = 0; i < types.length; i++) {
        Class<?> type = types[i];
        if (Object[].class.equals(type)) {
            result.add(params.parameterValues);
        }
    }
    
    //
    // Add the extra parameters we found
    //
    for (Object p : extraParameters) {
      result.add(p);
    }
    
    return result.toArray(new Object[result.size()]);
  }
  
  /**
   * If the method has parameters, fill them in.  Either by using a @DataProvider
   * if any was provided, or by looking up <parameters> in testng.xml
   * @return An Iterator over the values for each parameter of this
   * method.
   */
  public static ParameterHolder handleParameters(ITestNGMethod testMethod, 
      Map<String, String> allParameterNames,
      Object instance,
      MethodParameters methodParams, 
      XmlSuite xmlSuite, 
      IAnnotationFinder annotationFinder,
      Object fedInstance)
  {
    ParameterHolder result;

    Iterator<Object[]> parameters = null;
    
    //
    // Do we have a @DataProvider?  If yes, then we have several
    // sets of parameters for this method
    //
    DataProviderHolder dataProviderHolder =
        findDataProvider(testMethod.getTestClass().getRealClass(),
        testMethod.getMethod(), annotationFinder);

    if (null != dataProviderHolder) {
      int parameterCount = testMethod.getMethod().getParameterTypes().length;
  
      for (int i = 0; i < parameterCount; i++) {
        String n = "param" + i;
        allParameterNames.put(n, n);
      }

      parameters  = MethodHelper.invokeDataProvider(
          instance, /* a test instance or null if the dataprovider is static*/
          dataProviderHolder.method, 
          testMethod,
          methodParams.context,
          fedInstance,
          annotationFinder);

      Iterator<Object[]> filteredParameters = filterParameters(parameters,
          testMethod.getInvocationNumbers()); 

      result = new ParameterHolder(filteredParameters, ParameterHolder.ORIGIN_DATA_PROVIDER,
          dataProviderHolder);
    }
    else {
      //
      // Normal case:  we have only one set of parameters coming from testng.xml
      //
      allParameterNames.putAll(methodParams.xmlParameters);
      // Create an Object[][] containing just one row of parameters
      Object[][] allParameterValuesArray = new Object[1][];
      allParameterValuesArray[0] = createParameters(testMethod.getMethod(), 
          methodParams, annotationFinder, xmlSuite, ITestAnnotation.class, "@Test");
      
      // Mark that this method needs to have at least a certain
      // number of invocations (needed later to call AfterGroups
      // at the right time).
      testMethod.setParameterInvocationCount(allParameterValuesArray.length);
      // Turn it into an Iterable
      parameters  = MethodHelper.createArrayIterator(allParameterValuesArray);

      result = new ParameterHolder(parameters,
          allParameterValuesArray.length == 0 
              ? ParameterHolder.ORIGIN_DATA_PROVIDER : ParameterHolder.ORIGIN_XML,
          dataProviderHolder);
    }
    
    return result;
  }

  /**
   * If numbers is empty, return parameters, otherwise, return a subset of parameters
   * whose ordinal number match these found in numbers.
   */
  static private Iterator<Object[]> filterParameters(Iterator<Object[]> parameters,
      List<Integer> list) {
    if (list.isEmpty()) {
      return parameters;
    } else {
      List<Object[]> result = Lists.newArrayList();
      int i = 0;
      while (parameters.hasNext()) {
        Object[] next = parameters.next();
        if (list.contains(i)) result.add(next);
        i++;
      }
      return new ArrayIterator(result.toArray(new Object[list.size()][]));
    }
  }

  private static void ppp(String s) {
    System.out.println("[Parameters] " + s);
  }

  /** A parameter passing helper class. */
  public static class MethodParameters {
    private final Map<String, String> xmlParameters;
    private final Method currentTestMethod;
    private final ITestContext context;
    private Object[] parameterValues;
    public ITestResult testResult;
    
    public MethodParameters(Map<String, String> params) {
      this(params, null, null, null, null);
    }
    
    public MethodParameters(Map<String, String> params, Method m) {
      this(params, null, m, null, null);
    }
    
    public MethodParameters(Map<String, String> params, Object[] pv, Method m, ITestContext ctx,
        ITestResult tr) {
      xmlParameters = params;
      currentTestMethod = m;
      context = ctx;
      parameterValues = pv;
      testResult = tr;
    }
  }
}
