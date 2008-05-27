package org.testng.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.annotations.IConfiguration;
import org.testng.annotations.IDataProvider;
import org.testng.annotations.IFactory;
import org.testng.annotations.IParameterizable;
import org.testng.annotations.IParameters;
import org.testng.annotations.ITest;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlSuite;

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
                                                       ITestNGMethod currentTestMethod,
                                                       IAnnotationFinder finder, 
                                                       XmlSuite xmlSuite,
                                                       ITestContext ctx) 
  {
    Method currentTestMeth= currentTestMethod != null ? 
        currentTestMethod.getMethod() : null;
    return createParameters(m, new MethodParameters(params, currentTestMeth, ctx), 
        finder, xmlSuite, IConfiguration.class, "@Configuration");
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
      List<Object> vResult = new ArrayList<Object>();
  
      checkParameterTypes(methodName, parameterTypes, methodAnnotation, parameterNames);
  
      for(int i =0, j = 0; i < parameterTypes.length; i++) {
        if (Method.class.equals(parameterTypes[i])) {
          vResult.add(params.currentTestMethod);
        }
        else if (ITestContext.class.equals(parameterTypes[i])) {
          vResult.add(params.context);
        }
        else {
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
      if(!ITestContext.class.equals(parameterTypes[i])
          && !Method.class.equals(parameterTypes[i])) {
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
  
  private static Method findDataProvider(Class clazz, Method m, IAnnotationFinder finder) {
    Method result = null;
    String dataProviderName = null;
    Class dataProviderClass = null;
    
    ITest annotation = AnnotationHelper.findTest(finder, m);
    if (annotation != null) {
      dataProviderName = annotation.getDataProvider();
      dataProviderClass = annotation.getDataProviderClass();
    }
    
    if (dataProviderName == null) {
      IFactory factory = AnnotationHelper.findFactory(finder, m);
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
  private static Method findDataProvider(Class cls, IAnnotationFinder finder,
      String name, Class dataProviderClass) 
  {
    boolean shouldBeStatic = false;
    if (dataProviderClass != null) {
      cls = dataProviderClass;
      shouldBeStatic = true;
    }

    for (Method m : ClassHelper.getAvailableMethods(cls)) {
      IDataProvider dp = (IDataProvider) finder.findAnnotation(m, IDataProvider.class);
      if (null != dp && (name.equals(dp.getName()) || name.equals(m.getName()))) {
        if (shouldBeStatic && (m.getModifiers() & Modifier.STATIC) == 0) {
          throw new TestNGException("DataProvider should be static: " + m); 
        }
        
        return m;
      }
    }
    
    return null;
  }

  @SuppressWarnings({"deprecation"})
  private static Object[] createParameters(Method m, MethodParameters params,
      IAnnotationFinder finder, XmlSuite xmlSuite, Class annotationClass, String atName) 
  {
    List<Object> result = new ArrayList<Object>();
    
    Object[] extraParameters = new Object[0];
    //
    // Try to find an @Parameters annotation
    //
    IParameters annotation = (IParameters) finder.findAnnotation(m, IParameters.class);
    if(null != annotation) {
      String[] parameterNames = annotation.getValue();
      extraParameters = createParameters(m.getName(), m.getParameterTypes(), 
          finder.findOptionalValues(m), atName, finder, parameterNames, params, xmlSuite);
    }  
    
    //
    // Else, use the deprecated syntax
    //
    else {    
      IParameterizable a = (IParameterizable) finder.findAnnotation(m, annotationClass);
      if(null != a && a.getParameters().length > 0) {
        String[] parameterNames = a.getParameters();
        extraParameters = createParameters(m.getName(), m.getParameterTypes(), 
            finder.findOptionalValues(m), atName, finder, parameterNames, params, xmlSuite);
      }
      else {
        extraParameters = createParameters(m.getName(), m.getParameterTypes(), 
            finder.findOptionalValues(m), atName, finder, new String[0], params, xmlSuite);
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
  public static Iterator<Object[]> handleParameters(ITestNGMethod testMethod, 
                                                    Map<String, String> allParameterNames,
                                                    Object instance,
                                                    MethodParameters methodParams, 
                                                    XmlSuite xmlSuite, 
                                                    IAnnotationFinder annotationFinder,
                                                    Object fedInstance)
  {
    Iterator<Object[]> result = null;
    
    //
    // Do we have a @DataProvider?  If yes, then we have several
    // sets of parameters for this method
    //
    Method dataProvider = findDataProvider(testMethod.getTestClass().getRealClass(),
                                           testMethod.getMethod(), 
                                           annotationFinder);
  
    if (null != dataProvider) {
      int parameterCount = testMethod.getMethod().getParameterTypes().length;
  
      for (int i = 0; i < parameterCount; i++) {
        String n = "param" + i;
        allParameterNames.put(n, n);
      }

      result  = MethodHelper.invokeDataProvider(
          instance, /* a test instance or null if the dataprovider is static*/
          dataProvider, 
          testMethod,
          methodParams.context,
          fedInstance,
          annotationFinder);
    }
    else {
      //
      // Normal case:  we have only one set of parameters coming from testng.xml
      //
      allParameterNames.putAll(methodParams.xmlParameters);
      // Create an Object[][] containing just one row of parameters
      Object[][] allParameterValuesArray = new Object[1][];
      allParameterValuesArray[0] = createParameters(testMethod.getMethod(), 
          methodParams, annotationFinder, xmlSuite, ITest.class, "@Test");
      
      // Mark that this method needs to have at least a certain
      // number of invocations (needed later to call AfterGroups
      // at the right time).
      testMethod.setParameterInvocationCount(allParameterValuesArray.length);
      // Turn it into an Iterable
      result  = MethodHelper.createArrayIterator(allParameterValuesArray);
    }
    
    return result;
  }

  private static void ppp(String s) {
    System.out.println("[Parameters] " + s);
  }

  /** A parameter passing helper class. */
  public static class MethodParameters {
    private final Map<String, String> xmlParameters;
    private final Method currentTestMethod;
    private final ITestContext context;
    
    public MethodParameters(Map<String, String> params) {
      this(params, null, null);
    }
    
    public MethodParameters(Map<String, String> params, Method m) {
      this(params, m, null);
    }
    
    public MethodParameters(Map<String, String> params, Method m, ITestContext ctx) {
      xmlParameters= params;
      currentTestMethod= m;
      context= ctx;
    }
  }
}
